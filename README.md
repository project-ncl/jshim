# jshim
A simple application to manage having different versions of a tool for PNC.

A shim is an executable that sits between the command you type and the program you run. In our case, it mostly means
creating a symlink to the correct version of the tool we want to use. We might replace the symlink with a tiny Bash
script to be able to also setup any environment variables needed, and then call the actual tool.

Here's an example:
1. Add to your `PATH` the folder: `~/.locale/share/jshim/shims`
2. Add a symlink in the `shims` folder: `mvn` will now symlink to `/mnt/tools/maven/maven-3.9.9/bin/mvn`
3. Running `mvn` will now use the mvn 3.9.9 binary
4. If later on, we want to use Mvn 3.8.8 instead: we can make the symlink `mvn` point to `/mnt/tools/maven/maven-3.8.8/bin/mvn` instead

`jshim` has logic for specific tools, so that all the CLI binaries for an application (like Java) are symlinked properly.
In the case of Java, that means we'll have to add or adjust the shims for the CLI commands java, javac, javadoc, jps, etc.

## Tool folder structure
The tool folder structure where we symlink the shims **to** should have the structure:
```
<tool folder>/<tool>/<tool>-<version>
```

# TODO
- Add a post-hook script after the installation of a tool. Groovy looks fine for it
- Add support for mx, kotlin?
