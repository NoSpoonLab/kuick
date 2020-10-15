package kuick.samples.rpc.users

data class User(
    val userId: String,
    val name: String,
    val email: String,
    //val registeredAt: Long,
    val updatedAt: Long
)
