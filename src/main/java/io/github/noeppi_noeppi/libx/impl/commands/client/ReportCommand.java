package io.github.noeppi_noeppi.libx.impl.commands.client;

import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.noeppi_noeppi.libx.LibX;
import io.github.noeppi_noeppi.libx.command.CommandUtil;
import io.github.noeppi_noeppi.libx.impl.Executor;
import io.github.noeppi_noeppi.libx.impl.screen.ReportScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.StringUtils;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportCommand implements Command<CommandSourceStack> {

    public static final SuggestionProvider<CommandSourceStack> MOD_IDS = ((context, builder) -> SharedSuggestionProvider
            .suggest(ModList.get().getMods().stream().map(IModInfo::getModId).sorted(), builder));
    private static final Map<String, IModInfo> MOD_INFO_MAP = ModList.get().getMods().stream().collect(Collectors.toMap(IModInfo::getModId, info -> info));

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String modid = CommandUtil.getArgumentOrDefault(context, "modid", String.class, "").toLowerCase(Locale.ROOT);
        if (modid.isEmpty()) {
            // TODO modpack issue tracker
            return 0;
        }

        if (modid.equals("minecraft")) {
            context.getSource().sendSuccess(new TranslatableComponent("libx.commands.open_issue_mojang", "https://bugs.mojang.com/projects/MC/issues/"), false);
            return 0;
        }

        IModInfo modInfo = MOD_INFO_MAP.get(modid);
        if (modInfo == null) {
            context.getSource().sendFailure(new TranslatableComponent("You stupid, bro?"));
            return 0;
        }

        StringBuilder body = new StringBuilder("## Mods information\n");
        appendInfo(body, MOD_INFO_MAP.get("minecraft"));
        appendInfo(body, MOD_INFO_MAP.get("forge"));
        if (!modid.equals("forge")) {
            appendInfo(body, MOD_INFO_MAP.get(modid));
        }

        appendDependencies(Sets.newHashSet("minecraft", "forge", modid), body, modInfo);

        appendSection(body, "## Logs\n*insert log here via pastebin.com, gist.com, or any other service. For more information, visit https://git.io/mclogs#en*");
        appendSection(body, "## Description\n1. \n2. ");

        if (modInfo.getOwningFile() instanceof ModFileInfo fileInfo) {
            URL issueUrl = fileInfo.getIssueURL();
            if (modid.equals("forge")) {
                issueUrl = StringUtils.toURL("https://github.com/MinecraftForge/MinecraftForge/issues");
            }

            if (issueUrl == null) {
                context.getSource().sendSuccess(new TranslatableComponent("libx.command.information_collected").withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, body.toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click")))
                        .withColor(ChatFormatting.GOLD)
                        .withUnderlined(true)), false);
                return 0;
            }

            switch (IssueHost.getHost(issueUrl.getHost())) {
                case GITHUB -> {
                    String[] split = issueUrl.getPath().split("/");
                    int i = split[0].isEmpty() ? 1 : 0;
                    String username = split[i++];
                    String repo = split[i];

                    Executor.enqueueClientWork(() -> {
//                        PasteHandler pasteHandler = uploadLog();
//                        if (pasteHandler != null) {
//                            appendSection(body, "## Log\n" + pasteHandler.paste);
//                        }
//
//                        String repoUrl = "https://github.com/" + username + "/" + repo;
//                        String finalUrl = repoUrl + "/issues/new?body=" + URLEncoder.encode(body.toString(), StandardCharsets.UTF_8);
//                        ComponentLayout layout = ComponentLayout.simple(
//                                new TranslatableComponent("libx.command.open_issue_github"),
//                                new TextComponent(repoUrl).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)).withStyle(Style.EMPTY
//                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl))
//                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.link.open")))),
//                                new TextComponent("Delete Log").withStyle(Style.EMPTY
//                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, pasteHandler != null ? pasteHandler.edit : "Sorry, not possible")))
//                        );
                        Minecraft.getInstance().setScreen(new ReportScreen(new TranslatableComponent("libx.command.open_issue_github")));
                    });
                }
                case GITLAB -> {
                    String[] split = issueUrl.getPath().split("/");
                    int i = split[0].isEmpty() ? 1 : 0;
                    String username = split[i++];
                    String repo = split[i];

                    String repoUrl = "https://gitlab.com/" + username + "/" + repo;
                    String finalUrl = repoUrl + "/issues/new?issue[description]=" + URLEncoder.encode(body.toString(), StandardCharsets.UTF_8);
                    context.getSource().sendSuccess(new TranslatableComponent("libx.command.open_issue_gitlab", new TextComponent(repoUrl).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))).withStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.link.open")))), false);
                }
                case CUSTOM -> {
                    // TODO handle custom action
                }
            }
        }

        return 0;
    }

    private static void appendSection(StringBuilder builder, String information) {
        builder.append("\n").append(information).append("\n");
    }

    private static void appendInfo(StringBuilder builder, IModInfo info) {
        builder.append("**").append(info.getDisplayName()).append("**: ").append(info.getVersion()).append("\n");
    }

    private static void appendDependencies(Set<String> alreadyHandled, StringBuilder builder, IModInfo base) {
        for (IModInfo.ModVersion depVersion : base.getDependencies()) {
            String modid = depVersion.getModId();
            if (modid.equals("minecraft") || modid.equals("forge")) {
                continue;
            }

            IModInfo dependency = MOD_INFO_MAP.get(modid);
            if (dependency == null) { // mod not loaded
                continue;
            }

            appendInfo(builder, dependency);

            if (!alreadyHandled.contains(modid)) {
                alreadyHandled.add(modid);
                appendDependencies(alreadyHandled, builder, dependency);
            }
        }
    }

    private static PasteHandler uploadLog() {
        try {
            Path latestLog = FMLPaths.GAMEDIR.get().resolve("logs").resolve("latest.log");
            FileInputStream stream = new FileInputStream(latestLog.toFile());
            //noinspection ConstantConditions
            URI uri = URI.create("https://paste.melanx.de/create?title=" + Minecraft.getInstance().player.getDisplayName().getString());
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .POST(HttpRequest.BodyPublishers.ofString(readFromInputStream(stream)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return new PasteHandler(response.body());
        } catch (IOException | InterruptedException e) {
            return null;
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

    public enum IssueHost {
        GITHUB("github.com"),
        GITLAB("gitlab.com"),
        CUSTOM("");

        private final String hostUrl;

        IssueHost(String hostUrl) {
            this.hostUrl = hostUrl;
        }

        public boolean is(String hostUrl) {
            return this.hostUrl.equals(hostUrl.toLowerCase(Locale.ROOT));
        }

        public boolean is(URL url) {
            return this.hostUrl.equals(url.getHost().toLowerCase(Locale.ROOT));
        }

        public static IssueHost getHost(String hostUrl) {
            for (IssueHost host : IssueHost.values()) {
                if (host.is(hostUrl)) {
                    return host;
                }
            }

            return CUSTOM;
        }
    }
}
