package org.pac4j.core.matching.matcher.csrf;

import org.junit.Test;
import org.pac4j.core.context.MockWebContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.MockSessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.util.Pac4jConstants;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests {@link DefaultCsrfTokenGenerator}.
 *
 * @author Jerome Leleu
 * @since 1.8.0
 */
public final class DefaultCsrfTokenGeneratorTests {

    @Test
    public void testDefault() {
        final var generator = new DefaultCsrfTokenGenerator();
        internalTest(generator, true);
    }

    @Test
    public void testNoRotate() {
        final var generator = new DefaultCsrfTokenGenerator();
        generator.setRotateTokens(false);
        internalTest(generator, false);
    }

    @Test
    public void testRotate() {
        final var generator = new DefaultCsrfTokenGenerator();
        generator.setRotateTokens(true);
        internalTest(generator, true);
    }

    private void internalTest(final DefaultCsrfTokenGenerator generator, final boolean rotate) {
        final WebContext context = MockWebContext.create();
        final SessionStore sessionStore = new MockSessionStore();
        final var token = generator.get(context, sessionStore);
        assertNotNull(token);
        final var token2 = (String) sessionStore.get(context, Pac4jConstants.CSRF_TOKEN).orElse(null);
        assertEquals(token, token2);
        final long expirationDate = (Long) sessionStore.get(context, Pac4jConstants.CSRF_TOKEN_EXPIRATION_DATE).orElse(null);
        final var nowPlusTtl = new Date().getTime() + 1000 * generator.getTtlInSeconds();
        assertTrue(expirationDate > nowPlusTtl - 1000);
        assertTrue(expirationDate < nowPlusTtl + 1000);
        var newToken = generator.get(context, sessionStore);
        final var token3 = (String) sessionStore.get(context, Pac4jConstants.PREVIOUS_CSRF_TOKEN).orElse(null);
        final var token4 = (String) sessionStore.get(context, Pac4jConstants.CSRF_TOKEN).orElse(null);
        assertEquals(token, token3);
        assertEquals(token4, newToken);
        if (rotate) {
            assertNotEquals(token, token4);
        } else {
            assertEquals(token, token4);
        }
    }
}
