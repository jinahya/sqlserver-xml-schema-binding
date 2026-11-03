# CLAUDE.md

Jakarta XML Binding (XJC-generated) modules for the Microsoft SQL Server XML schemas listed on
https://schemas.microsoft.com/sqlserver/. Status of every namespace is recorded in `BINDING_STATUS.asciidoc`; keep it
updated whenever a module is added or changed. `BINDING_STATUS.asciidoc` is internal (never link it from `README.md`);
the module table in `README.md` (`targetNamespace`, description, artifactId) is the public one, so keep it in sync with
the table below.

## Module naming rule

One module per schema `targetNamespace`. Every name is derived from the namespace; never pick names by hand.

1. Take the `targetNamespace` **exactly as written in the schema** (not as listed on the index page; they differ for
   some), byte-exact: case and any trailing `/` kept, UTF-8, no trailing newline. Only when the schema has **no**
   `targetNamespace`, take the namespace as listed in the "XML Schema Namespace" column of the index page instead
   (`b872ce`, `b8b91e`); the generated classes then bind elements in no namespace, as the schema does.
2. token = `b` + first 4 lowercase hex characters of its SHA-256 + `e`.
3. module directory = artifactId = `sqlserver-xml-schema-binding-{token}`.
4. package = `Automatic-Module-Name` = `io.github.jinahya.sqlserver.xml.schema.binding.{token}`.

```sh
printf '%s' "http://schemas.microsoft.com/sqlserver/2004/07/showplan" | shasum -a 256 | cut -c1-4   # a1b3 -> ba1b3e
```

- The `b`/`e` wrap keeps the token a valid Java/JPMS identifier segment that neither starts nor ends with a digit.
- A new version of an existing namespace replaces the schema in the same module; it never gets a new module.
- Before adding a module, check the new token is not already taken (table below). On a collision, stop and ask.

## Module layout

- The module `pom.xml` defines `<module.name>io.github.jinahya.sqlserver.xml.schema.binding.{token}</module.name>`; the
  root
  `pom.xml` turns it into the package (`jaxb-maven-plugin` `generatePackage`) and the `Automatic-Module-Name`
  (`maven-jar-plugin`). Don't set packages in `.xjb` files.
- Schemas are Microsoft's copyrighted files with no redistribution grant: **never commit them or package them in a
  jar.** They are downloaded **manually**, never by the build: `./mvnw -N antrun:run@download-schemas` (root `pom.xml`,
  `<inherited>false</inherited>`, `<phase>none</phase>`) downloads `https://schemas.microsoft.com/{path}` to the root
  `schemas/{path}` (`*.xsd` git-ignored), skipping existing files, and fails on a SHA-256 mismatch. What to download
  is the committed index `schemas/schemas.sha256` (`shasum` format, `{sha256}  {path}`, two spaces; the single source
  of the hashes; `cd schemas && shasum -a 256 -c schemas.sha256` checks the files). A path also listed in the
  committed `schemas/schemas.wayback` (`{timestamp}  {path}`, for files now served as all zero bytes) is downloaded
  from that pinned Wayback Machine snapshot (`https://web.archive.org/web/{timestamp}id_/http://schemas.microsoft.com/{path}`)
  instead, still checked against `schemas.sha256`. Plain Ant has no loop, so the
  execution turns the index into a generated build file (`target/download-schemas.xml`) and runs it; no ant-contrib or
  `<script>`. It runs on the root only (`-N`):
  per-module antrun executions require dependency resolution, which fails before sibling modules are built.
- Each module sets `schema.path` in its `pom.xml` and declares `maven-antrun-plugin`, whose inherited `check-schema`
  execution (`initialize`) fails if `schema.path` isn't listed in the index or `schemas/{schema.path}` is missing. XJC
  reads only that file; tests needing the `.xsd` read `../schemas/...` (see `ShowPlanXMLTest`), not the classpath.
- Every generated class has value `equals`/`hashCode` (`jaxb-plugins` `-Xequals`/`-XhashCode`, root `pom.xml`; runtime
  dependency `jaxb-plugins-runtime`), through `ValueEqualsStrategy`/`ValueHashCodeStrategy`, which add DOM nodes
  (`isEqualNode`) to the JAXB strategies. They and `ValueEqualityTest` are written once in the root `src/templates/`
  and generated into each module's package (`generate-templates`, `@module.name@` replaced; not a shared module, so
  no split packages). `ValueEqualityTest` also unmarshals every `src/test/resources/**/*.xml` twice.
- Binding customizations (`*.xjb`) go under `src/main/resources/bindings/` (only when needed), with
  `schemaLocation="../../../../../schemas/{schema.path}"`.
- A schema importing another namespace without `schemaLocation`: resolve it with `src/main/catalog/catalog.xml`
  (`uri="../../../../schemas/{path}"`) and reuse the other module through its episode plus a Maven dependency (see
  `b2898e` -> `bdd36e`). Never generate a copy of another module's classes.
- Adding a module: compute the token, check it's free, add `<module>` to the root `pom.xml` (with the namespace
  comment),
  add `{sha256}  {path}` to `schemas/schemas.sha256`, set `module.name` and `schema.path`, declare
  `maven-antrun-plugin` and `jaxb-maven-plugin`, add an `ObjectFactoryTest`, and add the row to the tables here, in
  `README.md` and in `BINDING_STATUS.asciidoc`.

## Modules

| `targetNamespace`                                                                        | token (`sqlserver-xml-schema-binding-{token}`, `io.github.jinahya.sqlserver.xml.schema.binding.{token}`) |
|------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ContactInfo`             | `b7fdbe`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ContactRecord`           | `b80e2e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ContactTypes`            | `b2e7ae`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/IndividualSurvey`        | `b9c1be`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ProductModelDescription` | `b0fd0e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ProductModelManuInstructions` | `b73cae`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/ProductModelWarrAndMain` | `b4347e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/Resume`                  | `b3da3e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/adventure-works/StoreSurvey`             | `b95f5e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/dta`                                     | `b89eee`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/queryprocessor`                          | `b61cee`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/07/showplan`                                | `ba1b3e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/bulkload/format`                            | `bc42fe`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/sqltypes`                                   | `bdd36e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2006/11/eventdata` (as listed)                   | `b872ce`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/Options`                               | `bf91ee`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/types`                                 | `bdf87e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/types/SqlMessage`                      | `b8bc1e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/types/SqlParameter`                    | `b2898e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/types/SqlResultStream`                 | `bd5c8e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/types/SqlRowCount`                     | `b3f54e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2004/SOAP/types/SqlTransaction`                  | `bfe22e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2008/07/extendedeventsconfig`                    | `b1232e`                                                                                                 |
| `http://www.microsoft.com/SqlServer/Dts/DataTypeMapping.xsd`                             | `b1464e`                                                                                                 |
| `http://www.microsoft.com/SqlServer/Dts/DtwTypeConversion.xsd`                           | `b8e89e`                                                                                                 |
| `http://www.microsoft.com/SqlServer/Dts/Extensions.xsd`                                  | `b014de`                                                                                                 |
| `http://www.microsoft.com/SqlServer/Dts/UpgradeMapping.xsd`                              | `b985de`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2008/10/servicebroker/externalactivator`         | `b2707e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2008/DataDebugger/`                              | `bd290e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2011/geography`                                  | `b2a48e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/2012/upgradeadvisor/upgraderules` (as listed)    | `b8b91e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/dac/DeployReport/2012/02`                        | `b13d6e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/dac/DriftReport/2012/02`                         | `baccbe`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/dac/Serialization/2012/02`                       | `b669ce`                                                                                                 |
| `http://www.opengis.net/gml`                                                             | `bfdede`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/reporting/2005/01/reportdefinition`              | `be1d1e`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/reporting/2008/01/reportdefinition`              | `b2a2ce`                                                                                                 |
| `http://schemas.microsoft.com/sqlserver/reporting/2010/01/reportdefinition`              | `b9cace`                                                                                                 |

## Build

- `./mvnw -B -N antrun:run@download-schemas` once (schemas), then `./mvnw -B clean verify` builds and tests all
  modules; generated sources land in `target/generated-sources/xjc/`.
- Use the Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`): script-only (no
  `maven-wrapper.jar`), pinned to Maven 3.9.16 and verified by `distributionSha256Sum`. Stay on 3.9.x: Maven 3.10
  scopes server credentials by `<repositoryOrigins>`, which breaks deploying to Nexus. No `maven-wrapper-plugin` in the
  POMs; to change the version, edit `distributionUrl` and `distributionSha256Sum` (SHA-256 of the `-bin.zip`, checked
  against Apache's published `.sha512`). `.gitattributes` keeps `mvnw` LF and `mvnw.cmd` CRLF.
- Release: plain `./mvnw release:prepare release:perform`. `release:perform` builds a fresh checkout of the tag
  (`target/checkout`), which has no schemas, so the root `pom.xml` sets `<releaseProfiles>download-schemas</releaseProfiles>`
  (other release settings come from `jinahya-parent`); that profile binds `download-schemas` to the root's
  `initialize`. `./mvnw -Pdownload-schemas verify` does the same in one command.
- `.mvn/maven.config` holds default CLI options (`--threads=1C`; one argument per line). Parallel builds are safe
  because the build only reads `schemas/`; only `download-schemas` writes there.
- CI: `.github/workflows/build.yml` (Java 17/21/25) runs the same two commands, caching `schemas/**/*.xsd` in the
  Actions cache keyed on `schemas/schemas.sha256`, and fails if any `.xsd` is committed or packaged in a jar.
- Pin every plugin version in the root `pom.xml` (an unversioned plugin makes Maven resolve the latest, e.g. a 4.x
  beta).
- Every module has an `ObjectFactoryTest` (a `JAXBContext` loads). A module is `done` in `BINDING_STATUS.asciidoc` only
  with a sample-document round-trip test (see `ba1b3e`'s `ShowPlanXMLTest`, `b0fd0e`'s `ProductDescriptionTest`).

## Known pitfalls

- Schemas come from `https://schemas.microsoft.com/sqlserver/...`; they are used byte-for-byte unchanged (the SHA-256
  check enforces it), so fix problems with `.xjb` customizations instead. Some files there are served as all zero bytes
  (the original's size); look for a Wayback Machine snapshot of the same size
  (`https://web.archive.org/cdx/search/cdx?url=schemas.microsoft.com/sqlserver/{path}`) and list it in
  `schemas/schemas.wayback` (`b73cae`, `bdf87e`, `bfe22e`, `b014de`, `b985de`). Namespaces without any usable file
  (404, or no snapshot) are `skip` (reasons in `BINDING_STATUS.asciidoc`). A schema without a `targetNamespace` is not
  skipped (see the naming rule).
- A schema referencing a component of a namespace it doesn't import (`SqlRowSet`'s `xsd:schema` in `bdf87e`): XSOM
  fails with "undefined element declaration". Declare the component in a stub of our own under the root `src/stubs/`
  (`.xml`, as no `.xsd` is committed; `stubs.directory`), add it to the module's `jaxb-maven-plugin` `<schemas>`, and
  don't map its namespace (`map="false"`); modules importing that schema need the stub too (`bd5c8e`).
- XJC fails with an NPE (`CElement.getElementName()` is null) when mixed content references an element whose class
  comes from another module (episode, `<jaxb:class ref>` or `map="false"` alike). Bind those references with
  `<jaxb:dom/>` and the foreign namespace with `<jaxb:schemaBindings map="false"/>` (see `b0fd0e`).
- An untyped document element whose content type is named by a child (`EVENT_INSTANCE` of `b872ce`, typed by its
  `EventType`): JAXB can't pick the class, so the module has a hand-written helper (`EventInstances`), which also
  marshals without `xsi:type` (a context of the `ObjectFactory` knows the untyped element and adds one).
- Customizations in use: element/attribute name clash -> `<jaxb:property name="...Attribute"/>` (`ba1b3e`, `b669ce`);
  `_Ring`/`Ring` factory clash, and DataSet table/row classes of the same name -> `underscoreBinding="asCharInWord"`
  (`bfdede`, `b8b91e`); empty choice -> `<jaxb:class
  ref="java.lang.Object"/>` (`b2a2ce`).
- `MANIFEST.MF` wraps lines at 72 bytes; unfold continuation lines before checking `Automatic-Module-Name`.

## Repository

- GitHub: `git@github.com:jinahya/sqlserver-xml-schema-binding.git` (`main`, `develop`); POM `<url>`/`<scm>` match.
- Commit or push only when asked; ask which branch.
