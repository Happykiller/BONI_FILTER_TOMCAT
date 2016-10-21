# BONI_FILTER_TOMCAT
Ex filter

# Build the filter .jar
# Install in webapps\bonita\WEB-INF\lib
# Declare in webapps\bonita\WEB-INF\web.xml
    
	<filter>
	   <filter-name>filterFab</filter-name>
	   <filter-class>com.bonita.filter.tomcat.FilterFab</filter-class>
	</filter>
	
	<filter-mapping>
       <filter-name>filterFab</filter-name>
       <url-pattern>/portal/*</url-pattern>
       <url-pattern>/mobile/*</url-pattern>
       <url-pattern>/portal.js/*</url-pattern>
       <url-pattern>/apps/*</url-pattern>
       <url-pattern>/services/*</url-pattern>
	</filter-mapping>
