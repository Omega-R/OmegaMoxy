# OmegaMoxy
[![](https://jitpack.io/v/Omega-R/OmegaMoxy.svg)](https://jitpack.io/#Omega-R/OmegaMoxy)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)

Moxy is a library that helps to use MVP pattern when you do the Android Application. _Without problems of lifecycle and boilerplate code!_

The main idea of using Moxy:
![schematic_using](https://habrastorage.org/files/a2e/b51/8b4/a2eb518b465a4df9b47e68794519270d.gif)
See what's happening here in the [wiki](https://github.com/Arello-Mobile/Moxy/wiki).

## Capabilities

Moxy has a few killer features in other ways:
- _Presenter_ stay alive when _Activity_ recreated(it simplify work with multithreading)
- Automatically restore all that user see when _Activity_ recreated(including dynamic content is added)
- Capability to changes of many _Views_ from one _Presenter_

## Sample

View interface
```kotlin
interface HelloWorldView: MvpView {

    var waiting: Boolean

    @MoxyViewCommand(ONE_EXECUTION)
    fun showMessage(int message)

}
```
Presenter
```kotlin
class HelloWorldPresenter: MvpPresenter<HelloWorldView> {

    init {
        viewState.showMessage(R.string.hello_world)
    }

}
```
View implementation
```kotlin
class HelloWorldActivity: MvpAppCompatActivity, HelloWorldView {

    private val helloWorldPresenter: HelloWorldPresenter by providePresenter {
        HelloWorldPresenter()
    }

    private lateinit var helloWorldTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello_world)

	helloWorldTextView = findViewById<TextView>(R.id.activity_hello_world_text_view_message)
    }

    @Override
    override fun showMessage(message: Int) {
        helloWorldTextView.setText(message)
    }
}
```

[Here](https://github.com/Arello-Mobile/Moxy/tree/master/sample-github) you can see "Github" sample application.

## Wiki
For all information check [Moxy Wiki](https://github.com/Arello-Mobile/Moxy/wiki)

## Links
[References](https://github.com/Arello-Mobile/Moxy/wiki#references)<br />
[FAQ](https://github.com/Arello-Mobile/Moxy/wiki/FAQ)

## Integration

Add the JitPack repository to your build file:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add dependency:
```groovy

plugins {
  id 'com.google.devtools.ksp' version '1.9.0-1.0.11' apply false 
}

dependencies {
  ...
  implementation 'com.github.Omega-R.OmegaMoxy:moxy:3.1.0'
  implementation 'com.github.Omega-R.OmegaMoxy:moxy-androidx:3.1.0'
  ksp 'com.github.Omega-R.OmegaMoxy:moxy-compiler:3.1.0'	
}
```

## ProGuard
Moxy is completely without reflection! No special ProGuard rules required.

## License
```
The MIT License (MIT)

Copyright (c) 2023 Omega

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
