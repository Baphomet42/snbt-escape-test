package baphomethlabs.snbtescapetest.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

@Mixin(StringReader.class)
public abstract class StringReaderMixin {

    @Shadow
    protected abstract boolean canRead();

    @Shadow
    public abstract char read();

    @Shadow
    public abstract void setCursor(int cursor);

    @Shadow
    public abstract int getCursor();

    @Inject(
        method = "readStringUntil",
        at = @At(
            value = "HEAD"
        ),
        cancellable = true,
            remap = false
    )
    public void injectReadStringUntil(char terminator, CallbackInfoReturnable<String> cir) throws CommandSyntaxException {
        final StringBuilder result = new StringBuilder();
        boolean escaped = false;
        boolean returned = false;
        final StringBuilder unicodeSequence = new StringBuilder(4);
        boolean readUnicode = false;
        while (canRead() && !returned) {
            final char c = read();
            if(readUnicode) {
                if((""+c).matches("[a-f0-9]")) {
                    unicodeSequence.append(c);
                    if(unicodeSequence.length()==4) {
                        result.append((char)Integer.parseInt(unicodeSequence.toString(),16));
                        unicodeSequence.setLength(0);
                        readUnicode = false;
                    }
                }
                else {
                    String errorMsg = "u"+unicodeSequence.toString();
                    if(c != terminator)
                        errorMsg += c;
                    setCursor(getCursor() - 1 - unicodeSequence.length());
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(((StringReader)(Object)this), errorMsg);
                }
            }
            else if (escaped) {
                if (c == terminator || c == '\\')
                    result.append(c);
                else if (c == 'n')
                    result.append('\n');
                else if (c == 't')
                    result.append('\t');
                else if (c == 'r')
                    result.append('\r');
                else if (c == 'u')
                    readUnicode = true;
                else {
                    setCursor(getCursor() - 1);
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(((StringReader)(Object)this), String.valueOf(c));
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == terminator) {
                cir.setReturnValue(result.toString());
                returned = true;
            } else {
                result.append(c);
            }
        }

        if(!returned)
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(((StringReader)(Object)this));
        cir.cancel();
    }

}