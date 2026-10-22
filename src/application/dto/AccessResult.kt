package application.dto

sealed interface AccessResult {
  data object Success : AccessResult
  data object BadFormat : AccessResult
  data object BadLogin : AccessResult
  data object BadPassword : AccessResult
  data object UnknownAction : AccessResult
  data object NoResource : AccessResult
  data object NoAccess : AccessResult
  data object VolumeExceeded : AccessResult
}
