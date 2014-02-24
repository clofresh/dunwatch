### Dependencies

* [Eclipse](https://www.eclipse.org/)
* [Gradle](http://estiloasertivo.blogspot.com/2013/03/tutorial-howto-install-and-configure.html)

### Setting up Eclipse

Once you've installed all the Eclipse plugins, import the projects into Eclipse:

```
File -> Import... -> Gradle -> Gradle Project
Browse to the project root directory then click Build Model.
```

Eclipse may complain saying "SDK location not found." If that happens, create a file called `local.properties`:

```
sdk.dir = <path to Android SDK directory>
```

Once Build Model succeeds, select all the projects and click Finish.

Now you should be able to run the desktop version:

```
Right-click on the `dunwatch` folder -> Run As -> 2 Gradle Build...
In `Task execution order`, enter ":core:texturePacker, :desktop:run" then click Run
```
