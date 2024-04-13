package com.dyma.tennis.web;

import com.dyma.tennis.HealthCheck;
import com.dyma.tennis.Player;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Tag(name="Tennis Players API")
@RestController
public class PlayerController {

    //@Operation = Décrit ce que fait cette méthode
    @Operation(summary = "Finds players", description = "Finds players")
    //@ApiResponses = Indique les différentes responses que peut retourner notre méthode
    @ApiResponses(value = {
            //@ApiResponse = Documente la reponse retournée
            @ApiResponse(responseCode = "200", description = "Players list",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema (schema = @Schema(implementation = Player.class)))})
    })
    @GetMapping("/players")
    private List<Player> list(){
        return Collections.emptyList();
    }

    @Operation(summary = "Finds a player with lastName", description = "Finds player with lastName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))})
    })
    @GetMapping("/players/{lastName}")
    public Player getByLastName(@PathVariable("lastName") String lastName){
        return null;
    }
}
