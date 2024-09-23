package io.codemodder.sample;

import io.codemodder.testutils.CodemodTestMixin;
import io.codemodder.testutils.Metadata;

@Metadata(
        codemodType = UseJerseyDTOWhenPossibleCodemod.class,
        testResourceDir = "use-dto-instead-of-string",
        dependencies = {})
final class UseJerseyDTOWhenPossibleCodemodTest implements CodemodTestMixin { }
