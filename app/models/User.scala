package models

case class User(
  username: String, 
  password: String,
  email: String,
  profile: UserProfile
)

case class UserProfile(
  description: String,
  country: String,
  address: String,
  age: Int
)