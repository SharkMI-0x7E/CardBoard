package org.cardboardpowered.conflict;

import org.cardboardpowered.conflict.model.ConflictLevel;
import org.cardboardpowered.conflict.model.MixinClassInfo;
import org.cardboardpowered.conflict.model.MixinConflict;
import org.cardboardpowered.conflict.model.MixinMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConflictDetectorUnitTest {

    private MixinConflictDetector detector;

    @BeforeEach
    void setUp() {
        detector = new MixinConflictDetector();
    }

    @Test
    void testR1_doubleOverwriteFatalConflict() {
        MixinClassInfo cbInfo = createClassInfo(
                "net.minecraft.class_1234",
                "com.cardboard.mixin.CbMixin",
                "cardboard",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_a", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        MixinClassInfo otherInfo = createClassInfo(
                "net.minecraft.class_1234",
                "com.other.mixin.OtherMixin",
                "other-mod",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_a", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        List<MixinConflict> conflicts = detector.detect(Arrays.asList(cbInfo, otherInfo));

        assertFalse(conflicts.isEmpty(), "Should detect FATAL OVERWRITE_OVERWRITE conflict");
        MixinConflict c = conflicts.get(0);
        assertEquals(ConflictLevel.FATAL, c.level);
        assertEquals("OVERWRITE_OVERWRITE", c.conflictType);
    }

    @Test
    void testR2_overwriteVsInjectHighConflict() {
        MixinClassInfo cbInfo = createClassInfo(
                "net.minecraft.class_5678",
                "com.cardboard.mixin.CbOverwriteMixin",
                "cardboard",
                Collections.emptyList(),
                List.of(createOverwriteMethod("target_method", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        MixinClassInfo otherInfo = createClassInfo(
                "net.minecraft.class_5678",
                "com.other.mixin.OtherInjectMixin",
                "other-mod",
                List.of(createInjectMethod("target_method", 1000)),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        List<MixinConflict> conflicts = detector.detect(Arrays.asList(cbInfo, otherInfo));

        assertFalse(conflicts.isEmpty(), "Should detect HIGH OVERWRITE_INJECT conflict");
        MixinConflict c = conflicts.get(0);
        assertEquals(ConflictLevel.HIGH, c.level);
        assertEquals("OVERWRITE_INJECT", c.conflictType);
    }

    @Test
    void testSelfConflictFilter() {
        MixinClassInfo cbInfo1 = createClassInfo(
                "net.minecraft.class_9999",
                "com.cardboard.mixin.CbMixin1",
                "cardboard",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_x", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        MixinClassInfo cbInfo2 = createClassInfo(
                "net.minecraft.class_9999",
                "com.cardboard.mixin.CbMixin2",
                "cardboard",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_x", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        List<MixinConflict> conflicts = detector.detect(Arrays.asList(cbInfo1, cbInfo2));

        assertTrue(conflicts.isEmpty(), "Same mod conflicts should be filtered");
    }

    @Test
    void testEmptyInput() {
        List<MixinConflict> conflicts = detector.detect(Collections.emptyList());
        assertTrue(conflicts.isEmpty(), "Empty input should produce no conflicts");
    }

    @Test
    void testNoConflictWhenDifferentMethods() {
        MixinClassInfo cbInfo = createClassInfo(
                "net.minecraft.class_1111",
                "com.cardboard.mixin.CbMixin",
                "cardboard",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_a", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        MixinClassInfo otherInfo = createClassInfo(
                "net.minecraft.class_1111",
                "com.other.mixin.OtherMixin",
                "other-mod",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_b", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        List<MixinConflict> conflicts = detector.detect(Arrays.asList(cbInfo, otherInfo));
        assertTrue(conflicts.isEmpty(), "Different target methods should not conflict");
    }

    @Test
    void testFatalMixinSetNotEmptyAfterDetection() {
        MixinClassInfo cbInfo = createClassInfo(
                "net.minecraft.class_2222",
                "com.cardboard.mixin.FatalMixin",
                "cardboard",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_y", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        MixinClassInfo otherInfo = createClassInfo(
                "net.minecraft.class_2222",
                "com.other.mixin.OtherOverwrite",
                "other-mod",
                Collections.emptyList(),
                List.of(createOverwriteMethod("method_y", 1000)),
                Collections.emptyList(),
                Collections.emptyList()
        );

        detector.detect(Arrays.asList(cbInfo, otherInfo));

        Set<String> fatalSet = detector.getFatalMixinSet();
        assertFalse(fatalSet.isEmpty(), "FATAL mixin set should contain the Cardboard mixin");
        assertTrue(fatalSet.contains("com.cardboard.mixin.FatalMixin"));
    }

    // --- Helper methods ---

    private MixinClassInfo createClassInfo(String targetClass, String className, String modId,
                                           List<MixinMethod> injections, List<MixinMethod> overwrites,
                                           List<MixinMethod> redirects, List<MixinMethod> modifyArgs) {
        MixinClassInfo info = new MixinClassInfo();
        info.className = className;
        info.targetClasses = List.of(targetClass);
        info.sourceModId = modId;
        info.priority = 1000;
        info.isMixin = true;
        info.injections = injections;
        info.overwrites = overwrites;
        info.redirects = redirects;
        info.modifyArgs = modifyArgs;
        info.modifyVariables = Collections.emptyList();
        info.modifyReturnValues = Collections.emptyList();
        info.wrapWithConditions = Collections.emptyList();
        return info;
    }

    private MixinMethod createOverwriteMethod(String methodName, int priority) {
        MixinMethod method = new MixinMethod();
        method.name = methodName;
        method.annotationType = "Overwrite";
        method.targetMethods = List.of(methodName);
        method.priority = priority;
        method.atTargets = Collections.emptyList();
        return method;
    }

    private MixinMethod createInjectMethod(String methodName, int priority) {
        MixinMethod method = new MixinMethod();
        method.name = methodName + "$inject";
        method.annotationType = "Inject";
        method.targetMethods = List.of(methodName);
        method.priority = priority;
        method.atTargets = Collections.emptyList();
        return method;
    }
}
