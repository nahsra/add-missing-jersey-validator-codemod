package io.codemodder.sample;

import com.contrastsecurity.sarif.Result;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import io.codemodder.*;
import io.codemodder.codetf.DetectorRule;
import io.codemodder.javaparser.ChangesResult;
import io.codemodder.providers.sarif.semgrep.SemgrepSarifJavaParserChanger;
import io.codemodder.providers.sarif.semgrep.SemgrepScan;

import javax.inject.Inject;

import static io.codemodder.ast.ASTTransforms.addImportIfMissing;

/** Adds '@Valid' annotations to codemods. */
@Codemod(
        id = "pixee:java/add-missing-valid-assertions-to-jersey-codemod",
        reviewGuidance = ReviewGuidance.MERGE_WITHOUT_REVIEW,
        importance = Importance.MEDIUM,
        executionPriority = CodemodExecutionPriority.LOW)
public final class AddMissingValidAssertionsToJerseyCodemod
        extends SemgrepSarifJavaParserChanger<Parameter> {

    private static final String DETECTION_RULE =
            """
            rules:
            - id: unvalidated-pojo-dto-in-jersey-endpoint
              languages: [java]
              message: "POJO DTO parameter '$TYPE $NAME' in Jersey endpoint is not annotated with @Valid"
              severity: WARNING
              patterns:
                - pattern: |
                    @Path($PATH)
                    class $C {
                        ...
                        @POST
                        public $RET $METHOD(..., $TYPE $NAME, ...) {
                            ...
                        }
                        ...
                    }
                - pattern-not-inside: |
                    @Path($PATH)
                    class $C {
                        ...
                        @POST
                        public $RET $METHOD(..., @Valid $TYPE $NAME, ...) {
                            ...
                        }
                        ...
                    }
                - metavariable-regex:
                    metavariable: $TYPE
                    regex: '^(?!String$|Integer$|Boolean$|Byte$|Short$|Long$|Float$|Double$|Character$|Object$|List$|Set$|Map$|Date$|BigDecimal$|BigInteger$)[A-Z][a-zA-Z0-9]*$'
                - focus-metavariable: $TYPE
            """;

    @Inject
    public AddMissingValidAssertionsToJerseyCodemod(@SemgrepScan(yaml = DETECTION_RULE) final RuleSarif sarif) {
        super(sarif, Parameter.class, RegionNodeMatcher.MATCHES_START, CodemodReporterStrategy.fromClasspath(AddMissingValidAssertionsToJerseyCodemod.class));
    }

    @Override
    public DetectorRule detectorRule() {
        return new DetectorRule("unvalidated-pojo-dto-in-jersey-endpoint", "Unvalidated POJO DTO in Jersey endpoint", null);
    }

    @Override
    public ChangesResult onResultFound(
            final CodemodInvocationContext context,
            final CompilationUnit cu,
            final Parameter parameter,
            final Result result) {

        // Add the @Valid annotation to the parameter
        parameter.addAnnotation("Valid");
        addImportIfMissing(cu, "javax.validation.Valid");

        return ChangesResult.changesApplied;
    }
}
