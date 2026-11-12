package presentation.cli

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HelpPrinterTest {

  @Test
  fun `shouldContainAllArgumentDescriptions`() {
    assertTrue(HelpPrinter.helpText.contains("--login"))
    assertTrue(HelpPrinter.helpText.contains("--password"))
    assertTrue(HelpPrinter.helpText.contains("--action"))
    assertTrue(HelpPrinter.helpText.contains("--resource"))
    assertTrue(HelpPrinter.helpText.contains("--volume"))
    assertTrue(HelpPrinter.helpText.contains("--help"))
  }

  @Test
  fun `shouldContainAllExitCodesDescriptions`() {
    for (code in 0..8) {
      assertTrue(HelpPrinter.helpText.contains("$code —"), "Отсутствует описание кода $code")
    }
  }

  @Test
  fun `shouldNotContainLeadingMarginCharacters`() {
    assertFalse(HelpPrinter.helpText.startsWith("|"))
  }
}
