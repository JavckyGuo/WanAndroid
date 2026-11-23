package com.jacky.wanandroid.feature.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebUrlPolicyTest {
    @Test fun `upgrades cleartext article link to https`() {
        assertEquals("https://example.com/post?id=1", normalizeReaderUrl("http://example.com/post?id=1"))
    }

    @Test fun `keeps https article link unchanged`() {
        assertEquals("https://example.com/post", normalizeReaderUrl("https://example.com/post"))
    }

    @Test fun `allows web redirects across hosts`() {
        assertTrue(isWebUrl("https://cdn.example.com/article"))
        assertTrue(isWebUrl("http://legacy.example.com/article"))
    }

    @Test fun `blocks app and intent schemes`() {
        assertFalse(isWebUrl("intent://article#Intent;end"))
        assertFalse(isWebUrl("weixin://open"))
    }
}
