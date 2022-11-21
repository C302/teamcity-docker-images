/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.jetbrains.teamcity

// TODO: Optimize imports
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.vararg
import java.lang.IllegalArgumentException
import com.jetbrains.teamcity.common.constants.ValidationConstants
import com.jetbrains.teamcity.docker.DockerImage
import com.jetbrains.teamcity.docker.exceptions.DockerImageValidationException
import com.jetbrains.teamcity.docker.validation.ImageValidationUtils
import com.jetbrains.teamcity.docker.hub.data.DockerRegistryAccessor
import com.jetbrains.teamcity.teamcity.TeamCityUtils



/**
 * Subcommand for image validation. Will be consumed by ..
 * ... argument parser.
 */
class ValidateImage: Subcommand("validate", "Validate Docker Image") {
    val imageNames by argument(ArgType.String, description = "Images").vararg()
    var validated: Boolean = false

    /**
     * Execute image validation option specified via CLI.
     */
    override fun execute() {
        if (imageNames.size > 2) {
            throw IllegalArgumentException("Too much image names")
        }

        val registryAccessor = DockerRegistryAccessor("https://hub.docker.com/v2")
        val currentImage = DockerImage(imageNames[0])
        val size = registryAccessor.getSize(currentImage);
        TeamCityUtils.reportTeamCityStatistics("SIZE-${ImageValidationUtils.getImageStatisticsId(currentImage.toString())}", size)
        return

        // -- report image size to TeamCity
        val previousImageName = if (imageNames.size > 1) imageNames[1] else ""
        validated = ImageValidationUtils.validateSize(currentImage.toString(), previousImageName)
        if (!validated) {
            throw DockerImageValidationException("Image $currentImage size compared to previous ($previousImageName) " +
                    "suppresses ${ValidationConstants.ALLOWED_IMAGE_SIZE_INCREASE_THRESHOLD_PERCENT}% threshold.")
        }
    }
}

fun main(args: Array<String>) {

    val parser = ArgParser("automation")
    val imageValidation = ValidateImage()
    parser.subcommands(imageValidation)

    parser.parse(args)
}