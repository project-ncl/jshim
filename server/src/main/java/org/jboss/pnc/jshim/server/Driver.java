package org.jboss.pnc.jshim.server;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.api.dto.ComponentVersion;
import org.jboss.pnc.jshim.backend.constants.AutogeneratedConstants;
import org.jboss.pnc.jshim.backend.tools.BasicTool;
import org.jboss.pnc.jshim.backend.tools.ToolFactory;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class Driver {

    @ConfigProperty(name = "quarkus.application.name")
    String name;

    @Path("supported")
    @GET
    public Set<String> supported() {
        return ToolFactory.getToolsMap().keySet();
    }

    @Path("available/{toolName}")
    @GET
    public Response available(String toolName) {
        Map<String, BasicTool> toolMap = ToolFactory.getToolsMap();

        if (toolMap.containsKey(toolName)) {
            BasicTool tool = toolMap.get(toolName);
            return Response.ok().entity(tool.availableVersions().keySet().stream().toList()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("downloadable/{toolName}")
    @GET
    public Response downloadable(String toolName) {
        Map<String, BasicTool> toolMap = ToolFactory.getToolsMap();

        if (toolMap.containsKey(toolName)) {
            BasicTool tool = toolMap.get(toolName);
            return Response.ok().entity(tool.getDownloadableVersions()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Path("version")
    @GET
    public ComponentVersion getVersion() {
        return ComponentVersion.builder()
                .name(name)
                .version(AutogeneratedConstants.VERSION)
                .builtOn(ZonedDateTime.parse(AutogeneratedConstants.BUILD_TIME))
                .commit(AutogeneratedConstants.COMMIT_HASH)
                .build();
    }
}
