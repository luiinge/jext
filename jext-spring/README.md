jExt - Spring
================================================================================

This module is an extension to [jExt][1] that provides a new `ExtensionLoader` connected to
the Spring bean management. This way, every bean managed by Spring would be retrieved by the
**jExt** `ExtensionManager`.

Usage
-----------------------------------------------------------------------------------------
Simply include this library as a dependency. No further configuration is required.


### Maven dependency
Include the following within the `<dependencies>` section of your `pom.xml` file:
```xml
<dependency>
    <groupId>io.github.luiinge</groupId>
    <artifactId>jext-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Bean scopes
The extension instantiation is delegated to the Spring `ApplicationContext`, thus 
there is no guarantee that the jExt scopes are honour. In order to prevent unexpected behaviours,
any extension provided by this extension loader must include both jExt `@Extension` and 
Spring `@Scope` annotations in a way that their scope definitions match.

According to that, each scope must be defined as follows:
- `GLOBAL`
```java
@Extension(provider = "...", name = "...", version = "...", scope = GLOBAL)
@Scope("singleton")
@Component
public class MySpringComponent() {
    ...
}
```
- `LOCAL`
```java
@Extension (provider = "...", name = "...", version = "...", scope = LOCAL)
@Scope("prototype")
@Component
public class MySpringComponent() {
    ...
}
```
- `SESSION`
```java
@Extension (provider = "...", name = "...", version = "...", scope = SESSION)
@Scope("prototype")
@Component
public class MySpringComponent() {
    ...
}
```

Notice that, unintuitively, the `SESSION` scope should use `prototype` instead of `request`, 
`session`, or `global-session`. The `SpringExtensionLoader` will cache instances per session
when corresponding.



Authors
-----------------------------------------------------------------------------------------

- Luis Iñesta Gelabert  |  luiinge@gmail.com

Contributions
-----------------------------------------------------------------------------------------
If you want to contribute to this project, visit the
[Github project](https://github.com/luiinge/jext-spring). You can open a new issue
/ feature request, or make a pull request to consider. You will be added
as a contributor in this very page.

Issue reporting
-----------------------------------------------------------------------------------------
If you have found any defect in this software, please report it
in [Github project Issues](https://github.com/luiinge/jext-spring/issues).
There is no guarantee that it would be fixed in the following version but it would
be addressed as soon as possible.


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




[1]: <https://github.com/luiinge/jext>



