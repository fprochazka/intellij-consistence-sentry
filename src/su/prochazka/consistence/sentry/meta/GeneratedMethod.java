package su.prochazka.consistence.sentry.meta;

import com.intellij.openapi.project.Project;
import com.intellij.ui.RowIcon;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.PhpModifier;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpFieldIndex;

import org.jetbrains.annotations.NotNull;

import su.prochazka.consistence.sentry.util.StringUtil;

import javax.swing.*;

/**
 * @author Filip Prochazka <filip@prochazka.su>
 */
public class GeneratedMethod
{

    private String name;
    private String methodName;
    private PhpType type;
    private PhpModifier.Access visibility;
    private boolean isGetter;
    private boolean isSetter;

    public GeneratedMethod(String name, String methodName, PhpType type, PhpModifier.Access visibility, boolean isGetter, boolean isSetter)
    {
        this.name = name;
        this.methodName = methodName;
        this.type = type;
        this.visibility = visibility;
        this.isGetter = isGetter;
        this.isSetter = isSetter;
    }

    public PhpLookupElement toPhpLookupElement(@NotNull Project project, @NotNull ClassReference classReference)
    {
        PhpLookupElement item = new PhpLookupElement(
            formatName(),
            PhpFieldIndex.KEY,
            project,
            null
        );

        if (type != null) {
            item.typeText = type.toStringRelativized(classReference.getNamespaceName());
        }

        item.icon = new RowIcon(2) {{
            setIcon(PhpIcons.METHOD, 0);
            setIcon(getVisibilityIcon(), 1);
        }};

        return item;
    }

    private Icon getVisibilityIcon()
    {
        return (visibility != PhpModifier.Access.PUBLIC) ? (
            (visibility == PhpModifier.Access.PROTECTED)
                ? PhpIcons.PROTECTED
                : PhpIcons.PRIVATE
        ) : PhpIcons.PUBLIC;
    }

    private String formatName()
    {
        if (methodName != null) {
            return methodName;
        }

        if (isGetter) {
            return "get" + StringUtil.upperFirst(name);
        }

        if (isSetter) {
            return "set" + StringUtil.upperFirst(name);
        }

        throw new RuntimeException("Unexpected method type");
    }

}
