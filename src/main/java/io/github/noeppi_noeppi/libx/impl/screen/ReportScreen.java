package io.github.noeppi_noeppi.libx.impl.screen;

import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class ReportScreen extends Screen {

    private static final int SIZE_X = 200;
    private static final int SIZE_Y = 150;
    private Component logUrl = TextComponent.EMPTY;
    private Component deleteUrl = TextComponent.EMPTY;
    protected int leftPos;
    protected int topPos;

    public ReportScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - SIZE_X) / 2;
        this.topPos = (this.height - SIZE_Y) / 2;

        Button deleteButton = this.addRenderableWidget(new Button(this.leftPos + 50, this.topPos + 80, 60, 20, new TextComponent("Delete log"), button -> {
            this.deleteLog();
            this.logUrl = TextComponent.EMPTY;
            this.deleteUrl = TextComponent.EMPTY;
            button.active = false;
        }));
        deleteButton.active = false;

        this.addRenderableWidget(new Button(this.leftPos + 50, this.topPos + 50, 60, 20, new TextComponent("Upload log"), button -> {
            PasteHandler handler = uploadLog();
            if (handler != null) {
                this.logUrl = new TextComponent(handler.paste);
                this.deleteUrl = new TextComponent(handler.edit);
                deleteButton.active = true;
            }
        }));
    }

    @Override
    public void render(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderHelper.renderGuiBackground(poseStack, this.leftPos, this.topPos, SIZE_X, SIZE_Y);
        if (!this.logUrl.getContents().isEmpty()) {
            this.font.draw(poseStack, this.logUrl, this.leftPos + 13, this.topPos + 13, Color.LIGHT_GRAY.getRGB());
        }
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private static ReportScreen.PasteHandler uploadLog() {
        try {
            Path latestLog = FMLPaths.GAMEDIR.get().resolve("logs").resolve("latest.log");
            FileInputStream stream = new FileInputStream(latestLog.toFile());
            //noinspection ConstantConditions
            URI uri = URI.create("https://paste.melanx.de/create?title=" + Minecraft.getInstance().player.getDisplayName().getString());
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(readFromInputStream(stream)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return new ReportScreen.PasteHandler(response.body());
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private void deleteLog() {
        URI uri = URI.create(this.deleteUrl.getContents());
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }

        return resultStringBuilder.toString();
    }

    private static class PasteHandler {

        private final String paste;
        private final String edit;

        public PasteHandler(String jsonString) {
            String paste = "";
            String edit = "";

            try {
                JsonReader reader = new JsonReader(new StringReader(jsonString));
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("url")) {
                        paste = reader.nextString();
                    } else if (name.equals("edit")) {
                        edit = "https://paste.melanx.de/delete/" + reader.nextString();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } catch (IOException e) {
                LibX.logger.warn("Failed to create paste handler", e);
            }

            this.paste = paste;
            this.edit = edit;
        }
    }
}
