package su.prochazka.consistence.sentry.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import org.jetbrains.annotations.*;

import su.prochazka.consistence.sentry.meta.GeneratedMethod;
import su.prochazka.consistence.sentry.util.PhpIndexUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Filip Prochazka <filip@prochazka.su>
 */
public class SentryObjectMethodsCompletionContributor extends CompletionContributor
{

    private static PhpType sentryAwareType = new PhpType().add("Consistence\\Sentry\\SentryAware");

    public SentryObjectMethodsCompletionContributor()
    {
//        extend(
//            CompletionType.BASIC,
//            PlatformPatterns.psiElement().withParent(MemberReference.class),
//            new GeneratedAccessorsCompletionProvider()
//        );
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(ClassReference.class),
            new GeneratedAccessorsCompletionProvider()
        );
    }

    private class GeneratedAccessorsCompletionProvider extends CompletionProvider<CompletionParameters>
    {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet results)
        {
            PsiElement position = parameters.getPosition().getOriginalElement().getParent();
            if ((position instanceof ClassReference)) {
                addMethodCompletions(results, position, (ClassReference) position);
            }
        }

        private void addMethodCompletions(@NotNull CompletionResultSet results, @NotNull PsiElement position, @NotNull ClassReference classReference)
        {
            PhpType type = classReference.getType();
            PhpIndex phpIndex = PhpIndex.getInstance(position.getProject());
            if (!sentryAwareType.isConvertibleFrom(type, phpIndex)) {
                return;
            }

            for (GeneratedMethod method : findSentryMethods(classReference, phpIndex)) {
                results.addElement(
                    method.toPhpLookupElement(position.getProject(), classReference)
                );
            }
        }
    }

    private static List<GeneratedMethod> findSentryMethods(@NotNull ClassReference classReference, @NotNull PhpIndex phpIndex)
    {
        List<GeneratedMethod> methods = new ArrayList<>();
        for (PhpClass cls : PhpIndexUtil.getClasses(classReference, classReference.getProject())) {
            if (!sentryAwareType.isConvertibleFrom(cls.getType(), phpIndex)) {
                continue;
            }
            for (Field field : cls.getFields()) {
                if (!field.getModifier().isPrivate()) {
                    continue;
                }

                // todo: parse sentry annotations

                methods.add(new GeneratedMethod(
                    field.getName(),
                    null,
                    field.getType(),
                    field.getModifier().getAccess(),
                    true,
                    false
                ));

                methods.add(new GeneratedMethod(
                    field.getName(),
                    null,
                    field.getType(),
                    field.getModifier().getAccess(),
                    false,
                    true
                ));
            }
        }

        return methods;
    }

}
