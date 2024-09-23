This project holds multiple [codemodder](https://codemodder.io) codemods that help with enforcing missing validation in JAX-RS controller methods.

# Add Missing Jersey Validation

The first codemod adds missing `@Valid` annotations to JAX-RS controller methods.

```diff
  @Path("/example")
  public class MyController {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
-    public Response createMyDTO(MyDTO dto) {
+    public Response createMyDTO(@Valid MyDTO dto) {
        // my business logic here
        return Response.ok().build();
    }
  }
```

# Switch Manual String Deserialization to JAX-RS to Inject `@Valid`

The next codemod switches manually deserialized object patterns to using JAX-RS's built in deserialization, which would allow the framework to automatically apply validation.

```diff
  @Path("/example")
  public class MyController {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
-    public Response createMyDTO(String body) {
+    public Response createMyDTO(@Valid MyDTO dto) {
        // my business logic here
-       MyDTO dto = new ObjectMapper().readValue(body, MyDTO.class);
        doSomething(dto);
        return Response.ok().build();
    }
  }
```

# Setup

1. Install JDK 17 for building this project. We recommend [Eclipse Adoptium](https://adoptium.net/)

1. Install [Semgrep](https://semgrep.dev/) CLI. See
   [here](https://semgrep.dev/docs/getting-started/#installing-and-running-semgrep-locally)
   for instructions. It can usually be done via `pip`:
   ```shell
   pip install semgrep
   ```

If your Python library paths contain your home directory as a root folder (i.e.
due to the use of the `$HOME` environment variable), you may need to manually
set up your `PYTHONPATH` for tests:

```shell
PYTHONPATH=$HOME/<subpath-to-python-libs-folder> ./gradlew check
```

You can check your python paths with:

```shell
python -m site
```

# Testing

```bash
$ ./gradlew check
```

# Building

```bash
$ ./gradlew distZip
```

# Running

After building, you can run the distribution packaged in the `distZip` task.

```bash
$ cd app/build/distributions/
$ unzip app.zip
 
# do it without making the actual code changes on disk
$ app/bin/app --dry-run /my-project

# do it and make the actual code changes
$ app/bin/app /my-project
```
