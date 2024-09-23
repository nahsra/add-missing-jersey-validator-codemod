package io.codemodder.sample;

import com.contrastsecurity.sarif.Result;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import io.codemodder.*;
import io.codemodder.codetf.DetectorRule;
import io.codemodder.javaparser.ChangesResult;
import io.codemodder.providers.sarif.semgrep.SemgrepSarifJavaParserChanger;
import io.codemodder.providers.sarif.semgrep.SemgrepScan;

import javax.inject.Inject;

import java.util.Optional;

import static io.codemodder.ast.ASTTransforms.addImportIfMissing;
import static io.codemodder.javaparser.ASTExpectations.expect;

/** Turns endpoints that take a {@link String} and deserialize it manually into using JAX-RS DTO with validation. */
@Codemod(
        id = "pixee:java/use-dto-instead-of-string",
        reviewGuidance = ReviewGuidance.MERGE_WITHOUT_REVIEW,
        importance = Importance.MEDIUM,
        executionPriority = CodemodExecutionPriority.LOW)
public final class UseJerseyDTOWhenPossibleCodemod
        extends SemgrepSarifJavaParserChanger<VariableDeclarationExpr> {

    private static final String DETECTION_RULE =
            """
            rules:
              - id: use-dto-instead-of-string
                languages:
                  - java
                message: Use DTO as JAX-RS parameter instead of String with @Valid
                severity: WARNING
                patterns:
                  - pattern: |
                      @Path($PATH)
                      class $C {
                          ...
                          @POST
                          public $RET $METHOD(..., String $STR, ...) {
                              ...
                              $TYPE $NAME = (ObjectMapper $OM).readValue($STR);
                              ...
                          }
                          ...
                      }
                  - metavariable-regex:
                      metavariable: $TYPE
                      regex: ^(?!String$|Integer$|Boolean$|Byte$|Short$|Long$|Float$|Double$|Character$|Object$|List$|Set$|Map$|Date$|BigDecimal$|BigInteger$)[A-Z][a-zA-Z0-9]*$
                  - focus-metavariable: $TYPE
            """;

    @Inject
    public UseJerseyDTOWhenPossibleCodemod(@SemgrepScan(yaml = DETECTION_RULE) final RuleSarif sarif) {
        super(sarif, VariableDeclarationExpr.class, RegionNodeMatcher.MATCHES_START, CodemodReporterStrategy.fromClasspath(UseJerseyDTOWhenPossibleCodemod.class));
    }

    @Override
    public DetectorRule detectorRule() {
        return new DetectorRule("use-dto-instead-of-string", "Use DTO in Jersey endpoint to add validation", null);
    }

    @Override
    public ChangesResult onResultFound(
            final CodemodInvocationContext context,
            final CompilationUnit cu,
            final VariableDeclarationExpr varDecl,
            final Result result) {

        // get the variable declaration
        Optional<VariableDeclarator> dtoDeclarationRef = varDecl.getVariables().getFirst();
        if(dtoDeclarationRef.isEmpty()) {
            // can't find the variable declaration?
            return ChangesResult.noChanges;
        }

        // confirm the code is the right shape and grab the expected parts for modification
        VariableDeclarator dtoDeclaration = dtoDeclarationRef.get();
        Optional<Expression> initializer = dtoDeclaration.getInitializer();
        if(initializer.isEmpty()) {
            // no initializer where we expect the readValue() call to be
            return ChangesResult.noChanges;
        }

        Optional<MethodCallExpr> readValue = expect(initializer.get()).toBeMethodCallExpression().withName("readValue").result();
        if(readValue.isEmpty()) {
            // the initializer is not what we expected
            return ChangesResult.noChanges;
        }

        Optional<MethodDeclaration> methodDeclarationRef = dtoDeclaration.findAncestor(MethodDeclaration.class);
        if(methodDeclarationRef.isEmpty()) {
            // we can't find the parent method declaration?
            return ChangesResult.noChanges;
        }

        // add the variable declared to the method parameters
        MethodDeclaration methodDeclaration = methodDeclarationRef.get();
        Parameter parameter = methodDeclaration.addAndGetParameter(dtoDeclaration.getType(), dtoDeclaration.getNameAsString());
        parameter.addAnnotation("Valid");
        addImportIfMissing(cu, "javax.validation.Valid");

        // remove the string from the method parameters
        Expression argument = readValue.get().getArgument(0);
        methodDeclaration.getParameters().removeIf(p -> p.getNameAsString().equals(argument.toString()));
        dtoDeclaration.findAncestor(Statement.class).get().remove();

        // remove the creation of the ObjectMapper if it was created only for this
        return ChangesResult.changesApplied;
    }
}
