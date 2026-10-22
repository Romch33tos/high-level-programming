package infrastructure.storage

import domain.model.AccessRule
import domain.model.ByteArrayValue
import domain.model.PasswordHash
import domain.model.Resource
import domain.model.User
import infrastructure.utils.hexToBytes

object Storage {
  val users: List<User> = listOf(
    User(
      "alice",
      ByteArrayValue("df72903c72cccf1b259fab6274ae8b5d".hexToBytes()),
      PasswordHash("4b6b1a66e71e4489498eef4375dd4eff1ed6e809db10da98a38629a19051d5cd".hexToBytes())
    ),
    User(
      "bob",
      ByteArrayValue("4908214be0f7448a01072a1d2aa6ed5d".hexToBytes()),
      PasswordHash("82d7485d1e84936b193fd9ed6e324698597acdfce7a16b6ca0a2597c286b4d64".hexToBytes())
    ),
    User(
      "carol",
      ByteArrayValue("fbe50e9e4ed358e57e73a7d64eae36ad".hexToBytes()),
      PasswordHash("1b9d94c334c74569fb927f5c3ee7937f302b7a1e7f03558a6757a98e98ae7751".hexToBytes())
    )
  )

  val resources: List<Resource> = listOf(
    Resource("A", "", 100),
    Resource("B", "A", 50),
    Resource("C", "A.B", 25),
    Resource("D", "A", 10),
    Resource("E", "", 30)
  )

  val accessRules: List<AccessRule> = listOf(
    AccessRule("alice", "A", canRead = true, canWrite = true, canExecute = true),
    AccessRule("alice", "A.B", canRead = true, canWrite = false, canExecute = false),
    AccessRule("alice", "A.B.C", canRead = false, canWrite = true, canExecute = false),
    AccessRule("bob", "A", canRead = true, canWrite = false, canExecute = false),
    AccessRule("bob", "A.D", canRead = true, canWrite = true, canExecute = true),
    AccessRule("carol", "A.B.C", canRead = true, canWrite = true, canExecute = true)
  )
}
