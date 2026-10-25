package domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ActionTest {

  private fun rule(
    read: Boolean = false,
    write: Boolean = false,
    execute: Boolean = false
  ) = AccessRule("u", "A", read, write, execute)

  @Test
  fun `shouldParseReadActionIgnoringCase`() {
    assertEquals(Action.READ, Action.from("read"))
    assertEquals(Action.READ, Action.from("READ"))
    assertEquals(Action.READ, Action.from("Read"))
  }

  @Test
  fun `shouldParseWriteActionIgnoringCase`() {
    assertEquals(Action.WRITE, Action.from("write"))
    assertEquals(Action.WRITE, Action.from("WRITE"))
  }

  @Test
  fun `shouldParseExecuteActionIgnoringCase`() {
    assertEquals(Action.EXECUTE, Action.from("execute"))
    assertEquals(Action.EXECUTE, Action.from("EXECUTE"))
  }

  @Test
  fun `shouldReturnNullWhenActionIsUnknown`() {
    assertNull(Action.from("delete"))
    assertNull(Action.from(""))
    assertNull(Action.from("rea d"))
  }

  @Test
  fun `shouldPermitReadWhenRuleAllowsRead`() {
    assertTrue(Action.READ.isPermittedBy(rule(read = true)))
  }

  @Test
  fun `shouldDenyReadWhenRuleForbidsRead`() {
    assertFalse(Action.READ.isPermittedBy(rule(read = false, write = true, execute = true)))
  }

  @Test
  fun `shouldPermitWriteWhenRuleAllowsWrite`() {
    assertTrue(Action.WRITE.isPermittedBy(rule(write = true)))
  }

  @Test
  fun `shouldDenyWriteWhenRuleForbidsWrite`() {
    assertFalse(Action.WRITE.isPermittedBy(rule(read = true, execute = true)))
  }

  @Test
  fun `shouldPermitExecuteWhenRuleAllowsExecute`() {
    assertTrue(Action.EXECUTE.isPermittedBy(rule(execute = true)))
  }

  @Test
  fun `shouldDenyExecuteWhenRuleForbidsExecute`() {
    assertFalse(Action.EXECUTE.isPermittedBy(rule(read = true, write = true)))
  }
}
