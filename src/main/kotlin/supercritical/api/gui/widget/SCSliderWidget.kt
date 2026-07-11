package supercritical.api.gui.widget

import com.lowdragmc.lowdraglib.gui.texture.TextTexture
import com.lowdragmc.lowdraglib.gui.widget.SliderWidget
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import supercritical.api.gui.SCGuiTextures

/**
 * Control-rod slider for the fission reactor, porting the 1.12.2 `UpdatedSliderWidget` behavior:
 * the thumb tracks the live server-side insertion through a value provider, and dragging writes
 * the new value back through a responder.
 *
 * `handleClientAction` is overridden because LDLib 1.0.50's [SliderWidget] reads the synced
 * float twice on the server side (a quirk that breaks the stock slider-callback path); we read
 * it exactly once and forward it to the responder.
 *
 * The no-arg constructor lets LDLib mirror the widget on the client; the provider and responder
 * are only wired on the server-side instance built in `createUI`, which is the side that polls
 * the provider and reacts to `handleClientAction`.
 */
class SCSliderWidget : SliderWidget {
    private var responder: ((Float) -> Unit)? = null

    constructor() : super() {
        applyFissionLook()
    }

    constructor(x: Int, y: Int, width: Int, height: Int) : super(x, y, width, height) {
        applyFissionLook()
    }

    /** Polled each server tick so the thumb tracks the live control-rod insertion (0..1). */
    fun setProvider(provider: () -> Float): SCSliderWidget {
        setSliderValueProvider(provider)
        return this
    }

    /** Invoked server-side with the new insertion (0..1) when the player drags the slider. */
    fun setResponder(responder: (Float) -> Unit): SCSliderWidget {
        this.responder = responder
        return this
    }

    override fun initTemplate() {
        super.initTemplate()
        applyFissionLook()
    }

    override fun handleClientAction(id: Int, buffer: FriendlyByteBuf) {
        if (id == 1) {
            // Read exactly one float (see class kdoc) and forward it to the responder.
            responder?.invoke(buffer.readFloat())
        } else {
            super.handleClientAction(id, buffer)
        }
    }

    private fun applyFissionLook() {
        setMinAmount(0f)
        setMaxAmount(1f)
        handleTexture = SCGuiTextures.DARK_SLIDER_ICON
        handleHoverTexture = SCGuiTextures.DARK_SLIDER_ICON
        setBackground(SCGuiTextures.DARK_SLIDER_BACKGROUND)
        // The overlay label is resolved client-side each draw tick (TextTexture polls its
        // supplier). `%%` is escaped because TextTexture routes its text through String.format.
        setOverlay(
            TextTexture {
                Component.translatable(
                    "supercritical.gui.fission.control_rod_insertion",
                    String.format("%.2f%%", sliderValue * 100f)
                ).string.replace("%", "%%")
            }
        )
    }
}
