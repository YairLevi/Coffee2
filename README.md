# Coffee

Coffee is a light and quick package for writing desktop applications using familiar web technologies and frameworks.
Uses Webview and is inspired by the [wailsapp project](https://github.com/wailsapp/wails).
for a more detailed overview of the package, click [here](https://github.com/YairLevi/Coffee/docs/overview.md)

## Requirements
- Java 11 or above.
- Maven installed on you system. You can download [here](https://maven.apache.org/download.cgi).
- NodeJS and NPM (18.0+)


## Get Started
### CLI
If you have Golang installed, you can use:
```
go install github.com/YairLevi/Coffee/cli/coffee
```

Otherwise, you can download it from [here](https://github.com/YairLevi/Coffee/releases/download/0.1.9/coffee.rar).
Add the executable to some PATH folder, for convenience.

Usage is quick and easy:

* `init`  for creating new projects - 

  ```
  > coffee init <backend-template> <frontend-template>
  ```
* `dev`   for running in development mode -

  ```
  > coffee dev
  ```
* `build` for packaging the application into a `.jar` file - <br>

  ```
  > coffee build
  ```

<br>

### Manual
If you don't want to use the CLI tool, or add to an existing project.
Add the latest version you see on the release tab.

#### Maven

Add to `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<!-- ... -->

<dependencies>
    <dependency>
        <groupId>com.github.YairLevi</groupId>
        <artifactId>Coffee</artifactId>
        <version>latest_version</version>
    </dependency>
</dependencies>
```
#### Gradle 

Add it in your root build.gradle at the end of repositories:
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
Add dependency
```groovy
dependencies {
	implementation 'com.github.YairLevi:Coffee2:latest_version'
}
```


## Example
The code for both Java and Kotlin is pretty much the same.
```kotlin
import org.levi.coffee.Ipc
import org.levi.coffee.Window
import org.levi.coffee.annotations.BindMethod
import org.levi.coffee.annotations.BindType

@BindType
class MyClass(
    val name: String = "",
    var age: Int = 0,
    val hobbies: List<String> = emptyList(),
) {
    @BindMethod
    fun add(a: Int, b: Int): Int {
        return a + b
    }

    @BindMethod
    fun incrementAndInvoke() {
        age++;
        println("My age increased to $age")
        println("invoking event...")
        Ipc.invoke("event")
    }
}

fun main() {
    val win = Window()
    win.setSize(700, 700)
    win.setTitle("My first Coffee app!")
    win.setURL("http://localhost:5173")
    // Or some html... using win.setHTML("<!DOCTYPE...")
    win.bind(
        MyClass(),
    )
    win.addBeforeStartCallback { println("Started app...") }
    win.addOnCloseCallback { println("Closed the app!") }

    win.run()
}

```
