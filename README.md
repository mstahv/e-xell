# File Based Web Apps with Vaadin

This example application implements a tiny spreadsheet application.
You can create new files online, save them either to your own workstation or to browsers local storage, open them from your own workstation or from local storage.

The app is using the (commercial) Spreadsheet component to implement the "spreadsheet part" and there is an online version of this [deployed on a tiny demo server](http://vaadin-e-xell.dokku1.parttio.org/), but the main point of the example is not to brag with the component that implements the spreadsheet editor with a one-liner! Use Google Sheets if you need a proper online spreadsheet application.

The **beef of this example is the data is handled**. In typical web apps we use database on the server to store the data. In this web app, there is no database, it doesn't store anything on the filesystem nor to AWS S3 or similar. Users files are on their workstation, either on their normal file system or in their browser's local storage.

The fact that there is no persistence on the server naturally simplifies the architecture a bit. In this app we get the spreadsheet data serialized to bytes by Apache POI (that is used by Vaadin Spreadsheet), but in some more complex cases you could use for example JSON, XML, standard Java serialization and [Eclipse Serializer](https://vaadin.com/blog/you-might-not-need-the-database) to store custom data structures. It is not only about not needing Hibernate, but for example there is no need to add authorization, to save users specific data as that is handled by the user.

I have used this pattern, that I think should get a bit more exposure, in a couple of small web apps with good success. It suits well for cases where sharing, collaboration or access to your files from any browser is not needed. Also, the datasets that are handled in the app needs to be somewhat limited. It is for example a good fit for various utility apps, whose data sets are not in the cloud.

Some highlights from the code:

 * Downloading the contents of the Spreadsheet to a workstation as a file
 * Storing the same contents to the browsers "local storage". The WebStorage API only supports strings, so the app uses Base64 to encode the binary data. Before Base64 encoding, we also pass the byte stream through GZIPOutputStream to compress it. 
 * Reading the file input uploaded from the desktop
 * Reading the file input from the browser ("local storage"). Web Storage is essentially a string-string map, so we use special "files" key to store file names that user has used. There is no validation for the names, so you can probably break everything (at least your browsers local storage associated to this app), but saving your file with name "files" 👺

As a summary, here are some pro's and con's of this kind of web apps:

 * No need for a database.
 * No need for JPA or any other persistence libraries.
 * No hassle in deployment to connect the application server to the database.
 * No need to implement authentication to your application. Your users files are controlled by your users.
 * Sharing files with other users is both harder and more complex. In this example files only live in the JVM memory, associated to an open browser window, so it is impossible to share the file with others via server. But on the other hand, files can be sent as an email attachments or on floppy disks and even backed up on tape. And it is harder for non-skilled users to accidentally share important documents to the whole world ("to anyone with a link"), that is proven to happen with e.g. Google Docs. Of course, if the device with the browser (or filesystem) gets compromised, or the application servers memory, this kind of data can get hacked too.

## Running the application
The project is a standard Maven project. To run it from the command line,
type `mvn`, then open http://localhost:8080 in your browser.

You can also import the project to your IDE of choice as you would with any
Maven project. Read more on [how to set up a development environment for
Vaadin projects](https://vaadin.com/docs/latest/guide/install) (Windows, Linux, macOS).

## Deploying to Production
To create a production build, call `mvn clean package -Pproduction`.
This will build a JAR file with all the dependencies and front-end resources,
ready to be deployed. The file can be found in the `target` folder after the build completes.

Once the JAR file is built, you can run it using
`java -jar target/myapp-1.0-SNAPSHOT.jar` (NOTE, replace 
`myapp-1.0-SNAPSHOT.jar` with the name of your jar).

## Project structure

- `MainView.java` in `src/main/java` is an example Vaadin view.
- `src/main/resources` contains configuration files and static resources
- The `frontend` directory in the root folder is where client-side 
  dependencies and resource files should be placed.

## Useful links

- Read the documentation at [vaadin.com/docs](https://vaadin.com/docs).
- Follow the tutorials at [vaadin.com/tutorials](https://vaadin.com/tutorials).
- Watch training videos and get certified at [vaadin.com/learn/training]( https://vaadin.com/learn/training).
- Create new projects at [start.vaadin.com](https://start.vaadin.com/).
- Search UI components and their usage examples at [vaadin.com/components](https://vaadin.com/components).
- Find a collection of solutions to common use cases in [Vaadin Cookbook](https://cookbook.vaadin.com/).
- Find Add-ons at [vaadin.com/directory](https://vaadin.com/directory).
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/vaadin) or join our [Discord channel](https://discord.gg/MYFq5RTbBn).
- Report issues, create pull requests in [GitHub](https://github.com/vaadin/).
