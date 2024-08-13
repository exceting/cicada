package io.github.exceting.cicada.spring.demo;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public class OTELTest {
    public static void main(String[] args) {
        Tracer tracer = GlobalOpenTelemetry.getTracer("instrumentation-library-name","semver:1.0.0");
        Span span = tracer.spanBuilder("my span").startSpan();
        try (Scope scope = span.makeCurrent()) {
            // your use case
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR);
        } finally {
            span.end(); // closing the scope does not end the span, this has to be done manually
        }
    }
}
