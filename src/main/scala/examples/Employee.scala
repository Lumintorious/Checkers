// package examples

// import checkers.{given, *}

// type Name = String & NonEmpty & Alphabetic
// type Age = Int & Greater[16] & Less[70]
// type Amount = Long & Positive

// final case class Employee(
//   name: Name,
//   age: Age,
//   salary: Amount,
//   contactInfo: ContactInfo
// )

// final case class ContactInfo(
//   address: String,
//   phoneNumber: PhoneNumber,
//   email: Email
// )

// @main def employeeExample = {
//   val inputName = "John Doe"
//   val inputAge = 24
//   val inputSalary = 2000L
//   val inputAddress = "Some street 10th"
//   val inputPhoneNumber = "770-842-7380"
//   val inputEmail = "john.doe@gmail.com"
  
//   val employee = Employee.apply.checking(
//     inputName,
//     inputAge,
//     inputSalary,
//     ContactInfo.apply.checking(
//       inputAddress,
//       inputPhoneNumber,
//       inputEmail
//     )
//   )

//   (employee.fold(_.toList.mkString("\n"), _.toString))
// }