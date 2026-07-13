package io.github.symmetricdevs.supercritical.api.gui.widget

import com.lowdragmc.lowdraglib.gui.widget.SliderWidget
import net.minecraft.network.FriendlyByteBuf
import java.util.function.Consumer

/**
 * LDLib 1.0.50's [SliderWidget.handleClientAction] has a double-read bug at `id == 1`:
 * `sliderValue = readFloat(); sliderCallback.accept(sliderValue = readFloat())` — it reads the
 * synced float twice, while the client-side [setValue] writes only one. The second
 * read runs off the end of the 4-byte payload and throws during argument evaluation, so the
 * callback never fires and the slider value never lands on the server; the value-provider then
 * re-broadcasts the unchanged server state every tick, snapping the thumb back.
 *
 * This subclass skips the buggy super path for `id == 1`, reads exactly one float, and forwards it
 * to [responder]. The no-arg constructor is required so LDLib can mirror the widget on the client
 * (the responder is server-only; the client copy leaves it null and only ever *sends* actions).
 */
class ScritSliderWidget : SliderWidget {

    @Volatile
    private var responder: Consumer<Float>? = null

    constructor(x: Int, y: Int, w: Int, h: Int) : super(x, y, w, h)

    fun setResponder(responder: Consumer<Float>?) {
        this.responder = responder
    }

    override fun handleClientAction(id: Int, buffer: FriendlyByteBuf) {
        if (id == 1) {
            // Do NOT call super for id == 1 — it double-reads and exhausts the buffer.
            responder?.accept(buffer.readFloat())
            return
        }
        super.handleClientAction(id, buffer)
    }
}
