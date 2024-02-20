package com.example.test.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

@Getter
public class BearerToken extends AbstractAuthenticationToken {

    private final String value;

    public BearerToken(String value) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.value = value;
    }

    @Override
    public Object getCredentials() {
        return value;
    }

    @Override
    public Object getPrincipal() {
        return value;
    }
}
