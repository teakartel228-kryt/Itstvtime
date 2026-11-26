package com.tvtime.client;

import com.tvtime.TvTimeMod;
import com.tvtime.entity.AntTenna;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Mr. Ant-Tenna uses the supplied 64x64 skin on the zombie-sized humanoid model; size comes from the SCALE attribute (x1.6). */
public class TennaRenderer extends HumanoidMobRenderer<AntTenna, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private static final Identifier TEXTURE = TvTimeMod.id("textures/entity/tenna/ant_tenna.png");

	public TennaRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
	}

	@Override public HumanoidRenderState createRenderState() { return new HumanoidRenderState(); }
	@Override public Identifier getTextureLocation(HumanoidRenderState state) { return TEXTURE; }
}
