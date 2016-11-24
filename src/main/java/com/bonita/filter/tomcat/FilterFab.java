package com.bonita.filter.tomcat;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.console.common.server.utils.PermissionsBuilder;
import org.bonitasoft.console.common.server.utils.PermissionsBuilderAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.web.rest.model.user.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class FilterFab implements Filter {

    //com.bonita.filter.tomcat.FilterFab.level = FINEST
    public Logger logger = Logger.getLogger(FilterFab.class.getName());

    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    /**
     * chaque URL passe par cette méthode
     */
    public void doFilter(final ServletRequest request, final ServletResponse servletResponse, final FilterChain chain) throws IOException, ServletException {
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor(httpRequest);
        if (requestAccessor != null) {
            final APISession apiSession = requestAccessor.getApiSession();
            // si on a déjà une session, on ne fait rien (on passe au filtre suivant).
            if (apiSession != null) {
                logger.info("Already connected");
                chain.doFilter(httpRequest, servletResponse);
                return;
            }
        }

        String userName = "walter.bates";
        String password = "bpm";

        logger.fine("userName --> " + userName);

        if (userName == null || userName.trim().length() == 0) {
            logger.fine("userName is null");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // authentification par défaut
        }

        try {
            final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();

            // log in to the tenant to create a session
            final APISession apiSession = loginAPI.login(userName, password);
            // set the session in the TomcatSession
            logger.info("Connection success with[" + userName + "]");

            final HttpSession httpSession = httpRequest.getSession();
            final User user = new User(userName, Locale.FRENCH.getDisplayName());
            final PermissionsBuilder permissionsBuilder = PermissionsBuilderAccessor.createPermissionBuilder(apiSession);
            final Set<String> permissions = permissionsBuilder.getPermissions();
            SessionUtil.sessionLogin(user, apiSession, permissions, httpSession);
            chain.doFilter(httpRequest, servletResponse);
            return;

        } catch (final BonitaHomeNotSetException e) {
            logger.severe("Bonita Home is not set.");
        } catch (final ServerAPIException e) {
            logger.severe("ServerAPIException [" + e + "]");
        } catch (final UnknownAPITypeException e) {
            logger.severe("UnknownAPITypeException [" + e + "]");
        } catch (final LoginException e) {
            logger.severe("User[" + userName + "] LoginException");
        } catch (LoginFailedException e) {
            logger.severe("User[" + userName + "] LoginFailedException");
        }
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    public void destroy() {

    }

    public boolean isLoggable(LogRecord record) {
        return true;
    }
}
