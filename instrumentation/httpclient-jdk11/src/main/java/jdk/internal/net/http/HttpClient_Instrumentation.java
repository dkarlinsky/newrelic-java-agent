package jdk.internal.net.http;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Segment;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.Transaction;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.agent.instrumentation.httpclient.Util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Weave(originalName = "java.net.http.HttpClient", type = MatchType.BaseClass)
public abstract class HttpClient_Instrumentation {

    @Trace
    public <T> CompletableFuture<HttpResponse<T>>
    sendAsync(HttpRequest userRequest, HttpResponse.BodyHandler<T> responseHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {

        URI uri = userRequest.uri();
        Segment segment = null;
        if (uri != null) {
            String scheme = uri.getScheme().toLowerCase();
            Transaction txn = NewRelic.getAgent().getTransaction();

            // only instrument HTTP or HTTPS calls
            if (("http".equals(scheme) || "https".equals(scheme)) && txn != null) {
                segment = txn.startSegment("javahttpclient.sendAsync");
            }
        }

        CompletableFuture<HttpResponse<T>> completableFutureResponse = Weaver.callOriginal();
        if (segment == null) {
            return completableFutureResponse;
        }
        return completableFutureResponse.whenComplete(Util.reportAsExternal(uri, segment));
    }
}



