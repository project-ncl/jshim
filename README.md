# jshim
A simple application to manage having different versions of a tool for PNC.

A shim is an executable that sits between the command you type and the program you run. In our case, it mostly means
creating a symlink to the correct version of the tool we want to use. We might replace the symlink with a tiny Bash script to be able to also setup any environment variables needed, and then call the actual tool.

Here's an example:
1. Add a symlink in the `shims` folder: `mvn` will now symlink to `/mnt/tools/maven/maven-3.9.9/bin/mvn`
2. Add to your shell's `PATH` the `shims` folder: `~/.locale/share/jshim/shims`
   ```bash
   $ export PATH=~/.locale/share/jshim/shims:$PATH
   ```
3. Running `mvn` will now use the mvn 3.9.9 binary
4. If later on, we want to use Mvn 3.8.8 instead: we can make the symlink `mvn` point to `/mnt/tools/maven/maven-3.8.8/bin/mvn` instead

`jshim` handles the symlinking part, as well as downloading the tools' binary versions. It has logic for specific tools, so that all the CLI binaries for an application (like Java) are symlinked properly.
In the case of Java, that means we'll have to add or adjust the shims for the CLI commands java, javac, javadoc, jps, etc.

## Customization
By default, the shim and downloaded binaries are in:
- downloaded binaries: `<jshim data path>/downloaded/<tool>/<tool>-<version>`
- shims: `<jshim data path>/shims/<tool>`

We can specify where the shims and downloaded tool versions are by using the environment variables:
- `JSHIM_DATA_PATH`: the path where the downloaded tools will be placed
- `JSHIM_SHIM_PATH`: the path where the `shims` folder will be placed

This is useful for the scenario where the data path is a volume shared between different pods, and the "shim path" is
set to the pod's local storage. This allows each pod to customize its own tool version while benefiting from
the shared volume and not having to constantly download different binary versions.

Example:
```bash
$ export JSHIM_DATA_PATH=/mnt/shared/jshim
$ export JSHIM_SHIM_PATH=/home/user/.local/share/shims
```
We'll then need to add the `JSHIM_SHIM_PATH` to our shell's path:
```bash
$ export PATH=$JSHIM_SHIM_PATH:$PATH
```

## Home Environment Variable
Some tools like `java` also needs to define an environment variable (`JAVA_HOME` for `java`) to work optimally.

This is done by creating a symlinked folder inside the `shims` folder which points to the home path of the tool version.

To export those environment variables, we need to run:
```bash
$ source ~/${JSHIM_SHIM_PATH}/source-file.sh
```
in your terminal shell. The exported environment variables point to the symlinked folder, which in turns point to the specific tool version
folder.

After this, switching between `java` versions shouldn't require the user to run the `source`
command again since the home symlinked folder will be adjusted automatically. However, installing new tools will require
the user to run the `source` command again to capture the new home environment variables
to export.

## Tool Folder Structure
The tool folder structure where we symlink the shims **to** has the structure:
```
<jshim data path>/downloaded/<tool>/<tool>-<version>
```


# TODO
- Add a post-hook script after the installation of a tool. Groovy looks fine for it
- Add support for mx, kotlin?
