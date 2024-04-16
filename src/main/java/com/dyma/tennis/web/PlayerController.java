package com.dyma.tennis.web;

import com.dyma.tennis.HealthCheck;
import com.dyma.tennis.Player;
import com.dyma.tennis.PlayerList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(name="Tennis Players API")
@RestController
@RequestMapping("/players")
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
    @GetMapping
    public List<Player> list(){
        return PlayerList.ALL;
    }

    @Operation(summary = "Finds a player with lastName", description = "Finds player with lastName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))})
    })
    @GetMapping("{lastName}")
    public Player getByLastName(@PathVariable("lastName") String lastName){
        return null;
    }

    @Operation(summary = "Creates a player", description = "Creates a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created a player",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))})
    })
    @PostMapping
    public Player Player(@RequestBody Player player){
        return player;
    }

    @Operation(summary = "Updates a player", description = "Updates a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated a player",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))})
    })
    @PutMapping
    public Player updatePlayer(@RequestBody Player player){
        return player;
    }

    @Operation(summary = "Deletes a player", description = "Deletes a player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Player has been deleted")
    })
    @DeleteMapping("{lastName}")
    public void DeletePlayerByLastName(@PathVariable("lastName") String lastName){

    }
}
