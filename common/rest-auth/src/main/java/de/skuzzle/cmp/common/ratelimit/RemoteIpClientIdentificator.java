package de.skuzzle.cmp.common.ratelimit;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public class RemoteIpClientIdentificator implements ClientIdentificator<HttpServletRequest> {

    @Override
    public Optional<ApiClient> tryIdentifyClientFrom(HttpServletRequest request) {
        final String remoteIp = getClientIP(request);
        return Optional.of(new SimpleApiClient(remoteIp));
    }

    private String getClientIP(HttpServletRequest request) {
        final String xrealIp = request.getHeader("X-Real-Ip");
        if (xrealIp == null) {
            return request.getRemoteAddr();
        }
        return xrealIp;
    }

}
