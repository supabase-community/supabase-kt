# Supabase-kt Compose Auth UI

Extends Supabase-kt with UI composables

Newest version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.supabase/supabase-kt)](https://search.maven.org/search?q=g%3Aio.github.jan-tennert.supabase)

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:VERSION")
}
```

# Full Example

<details>

<summary>Full Compose for Desktop Example</summary>

```kotlin
singleWindowApplication {
    MaterialTheme(
        darkColorScheme()
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            AuthForm {
                var password by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                val state = LocalAuthState.current
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedEmailField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail") },
                        mandatory = email.isNotBlank() //once an email is entered, it is mandatory. (which enable validation)
                    )
                    OutlinedPhoneField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") }
                    )
                    OutlinedPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        rules = rememberPasswordRuleList(PasswordRule.minLength(6), PasswordRule.containsSpecialCharacter(), PasswordRule.containsDigit(), PasswordRule.containsLowercase(), PasswordRule.containsUppercase())
                    )
                    FormComponent("accept_terms") { valid ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = valid.value,
                                onCheckedChange = { valid.value = it },
                            )
                            Text("Accept Terms", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    Button(
                        onClick = {}, //Login with email and password,
                        enabled = state.validForm,
                    ) {
                        Text("Login")
                    }
                    OutlinedButton(
                        onClick = {}, //Login with Google,
                        content = { ProviderButtonContent(Google) }
                    )
                    Button(
                        onClick = {}, //Login with Twitch,
                        content = { ProviderButtonContent(Twitch) }
                    )
                }
            }
        }
    }
}
```

https://github.com/supabase-community/supabase-kt/assets/26686035/91e5d533-3b01-4093-9585-a35e59b66927

</details>

# Usage

### PasswordField
You can set `rules` for the password like this:
```kotlin
OutlinedPasswordField(
    value = password,
    onValueChange = { password = it },
    label = { Text("Password") },
    rules = rememberPasswordRuleList(PasswordRule.minLength(6), PasswordRule.containsSpecialCharacter(), PasswordRule.containsDigit(), PasswordRule.containsLowercase(), PasswordRule.containsUppercase())
)
```
Once set, the field will automatically display an error supporting text if the password doesn't match the criteria.
Note that the built-in roles also accept a custom description as a parameter.
You can also easily create your own rules:
```kotlin
val myRule = PasswordRule("description") { password ->
    password.contains("-")
}
```

### EmailField
The email field has a validator parameter which defaults to a regex solution.
Example:
```kotlin
OutlinedEmailField(
    value = email,
    onValueChange = { email = it },
    label = { Text("E-Mail") },
    validator = EmailValidator { 
        //validate email
    }
)
```

### Phone Field
The phone field also has a validator parameter, which just checks whether the phone number only consists of digits.
On top of that you can provide a `mask` parameter, which changes how the phone number gets displayed. The value is just the raw number.
Example with the default mask:
```kotlin
OutlinedPhoneField(
    value = phone,
    onValueChange = { phone = it },
    label = { Text("Phone") }
)
```

![image](https://github.com/supabase-community/supabase-kt/assets/26686035/5405772b-f6f8-45e7-a28d-a55003f48e75)

Example with custom mask: (you can also set it to null, if you don't want a mask)
```kotlin
OutlinedPhoneField(
    value = phone,
    onValueChange = { phone = it },
    label = { Text("Phone") },
    mask = "(###) ###-####"
)
```

![image](https://github.com/supabase-community/supabase-kt/assets/26686035/13251358-9147-4f49-8116-9776ec3266b8)

**Note: You can customize the fields completely, they just all have default values. They also have a `mandatory´ option, you can use that to have optional fields. It is also possible to make fields only mandatory (=validation enabled) once the field is not empty. (See example)**

### Provider Button
This module provides a function to generate the button content independently of the variation:
```kotlin
OutlinedButton(
    onClick = {}, //Login with Google,
    content = { ProviderButtonContent(Google) }
)
Button(
    onClick = {}, //Login with Twitch,
    content = { ProviderButtonContent(Twitch) }
)
```

![image](https://github.com/supabase-community/supabase-kt/assets/26686035/fb5263e7-272c-4a79-a3c4-1f6755922752)

You can also only use the icon if you want a custom layout:
```kotlin
Button(
    onClick = {},
) {
    ProviderIcon(Google)
    Text("Login with Google")
}
```

### Custom Form Components
You can also easily make your own form components:
```kotlin
FormComponent(formKey = "accept_terms", mandatory = true) { valid ->
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = valid.value,
            onCheckedChange = { valid.value = it },
        )
        Text("Accept Terms", color = MaterialTheme.colorScheme.onBackground)
    }
}
```
Mandatory changes whether the `AuthState#validForm` property is affected by this component.