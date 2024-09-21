package io.codemodder.sample;

import io.codemodder.testutils.CodemodTestMixin;
import io.codemodder.testutils.Metadata;

@Metadata(
        codemodType = AddMissingValidAssertionsToJerseyCodemod.class,
        testResourceDir = "add-valid-to-jersey-dto",
        dependencies = {})
final class AddMissingValidAssertionsToJerseyCodemodTest implements CodemodTestMixin { }
