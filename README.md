jExt
================================================================================

jExt is a simple library that allows you to define a *plug-in* architecture detecting the
Java classes that implements a specific interface. It mostly relies on the standard
[extension mechanism provided][1] by Java by means of the [ServiceLoader][2]
class, but it simplifies and enhances the usage.

The key concepts are:

- **Extension points** (interfaces annotated with `@ExtensionPoint`) which define the contract
- **Extensions** (classes annotated with `@Extension` and implementing the extension point class)
- **Extension loaders** that are responsible of discovering extensions for an specific extension point

What is the extra value then?

- Annotation processing of `@Extension` and `@ExtensionPoint` so the classes are
automatically registered as services by the compiler
- Capability of using multiple class loaders at the same time
- Use the default `ServiceLoader` mechanism or provide your own discoverer from a IoC framework
- Explicitly declaring the extension point version that implement each extension,
avoiding using wrong jar versions
- Custom control about creating new extension instances or reusing existing ones
- When extending an extension via inheritance, you may either override the parent class or
use both superclass and subclass independently


Usage
-----------------------------------------------------------------------------------------
The usage of **jExt** can be split in three roles: the extension point declarer, the extension
provider, and the extension consumer. Those roles can be played by the same actor or different ones,
but the usage do not vary.

### Declaring a extension point
Simply annotate an interface with `@Extension`:

```java
@Extension
public interface MyExtensionPoint {

    List<String> provideStuff();

}
```

### Providing an extension
Simply annotate a class implementing the interface with `@ExtensionPoint`:

```java
@ExtensionPoint
public class MyExtension implements MyExtensionPoint {

    @Override
    public List<String> provideStuff() {
        return List.of("apple","carrot","lemon");
    }

}
```


### Consuming an extension point
Use a `ExtensionManager` instance to get one or many implementations of the extension point:

```java
    ExtensionManager extensionManager = new ExtensionManager();
    Optional<MyExtensionPoint> extension = extensionManager.getExtension(MyExtensionPoint.class);
```

You can apply several filters (predicates) when asking for an extension, such as provider or
specific versions.


### Versioning

One major improvement over the regular `ServiceLoader` is avoiding version mismatches. Since
Java does not have the concept of *version* at runtime, you can end up using an implementation
version that is not aligned with the interface version. Thus, unexpected errors like
`NoSuchMethodError` may occur without any further hint of what is happening.

Using **jExt** you can provide extra information in the annotations that helps to determine if an
extension is suitable for an extension point. The `ExtensionPoint` annotation has the property
`version`, used in the form of `<major>.<minor>[.<patch>]`, and the `Extension` annotation
has the property `extensionPointVersion` in the same manner. If the existing extension uses
an extension point version that is not compatible with the actual extension point version, it
will not be selected by the `ExtensionManager` preventing potential errors.

If you do not care about versioning, just ignore it; version `1.0.0` will be used by default.


### Other considerations

#### Java modules
When Jigsaw module system is present, extension points and extensions must be declared manually
in your `module-info.java` file using the `uses`and `provides` statements. For **jExt** to
automatically manage that, a byte-code manipulator is required. Experiments regarding this feature
are planned and it is likely that it would be present in a future version.



### Maven dependency
Include the following within the `<dependencies>` section of your `pom.xml` file:
```xml
<dependency>
    <groupId>jext</groupId>
    <artifactId>jext</artifactId>
    <version>1.0.0</version>
</dependency>
```

Use the following repository to obtain this artifact:
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://luiinge.github.io/maven-repo</url>
    </repository>
</repositories>
```

License
-----------------------------------------------------------------------------------------

```
    MIT License

    Copyright (c) 2020 Luis Iñesta Gelabert - luiinge@gmail.com

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


Authors
-----------------------------------------------------------------------------------------

- Luis Iñesta Gelabert  |  luiinge@gmail.com


Contributions
-----------------------------------------------------------------------------------------
If you want to contribute to this project, visit the
[Github project](https://github.com/luiinge/jext). You can open a new issue / feature
request, or make a pull request to consider. If your contribution is worthing, you will be added
as a contributor in this very page.




[1]: <https://docs.oracle.com/javase/tutorial/ext/basics/spi.html>
[2]: <https://docs.oracle.com/javase/11/docs/api/java/util/ServiceLoader.html>


