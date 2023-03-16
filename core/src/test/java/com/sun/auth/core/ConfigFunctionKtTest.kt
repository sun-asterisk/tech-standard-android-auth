package com.sun.auth.core

import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class ConfigFunctionKtTest {
    class TestConfig {
        var value: Int = 0

        companion object {
            fun apply(value: Int, setup: ConfigFunction<TestConfig>): TestConfig {
                return TestConfig().apply {
                    this.value = value
                    setup.invoke(this)
                }
            }
        }
    }

    @Test
    fun testInvoke() {
        val setup: TestConfig.() -> Unit = spyk()
        val config = TestConfig.apply(1, invoke(setup))
        verify { setup.invoke(config) }
        assert(config.value == 1)
    }
}
