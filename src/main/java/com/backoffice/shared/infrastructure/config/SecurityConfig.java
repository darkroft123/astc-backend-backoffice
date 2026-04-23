package com.backoffice.shared.infrastructure.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

@LoginConfig(authMethod = "MP-JWT")
public class SecurityConfig extends Application {
}
