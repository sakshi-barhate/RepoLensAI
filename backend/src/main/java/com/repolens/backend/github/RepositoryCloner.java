package com.repolens.backend.github;

import com.repolens.backend.exception.RepositoryNotFoundException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.TransportException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RepositoryCloner {

    public File cloneRepository(String repositoryUrl) {

        try {

            String repositoryName = repositoryUrl
                    .substring(repositoryUrl.lastIndexOf("/") + 1)
                    .replace(".git", "");

            Path tempDirectory =
                    Files.createTempDirectory("repolens-");

            File repositoryDirectory =
                    new File(tempDirectory.toFile(), repositoryName);

            System.out.println(
                    "Cloning repository: " + repositoryUrl
            );

            System.out.println(
                    "Clone location: " +
                    repositoryDirectory.getAbsolutePath()
            );

            Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(repositoryDirectory)
                    .setDepth(1)
                    .call();

            System.out.println(
                    "Repository cloned successfully!"
            );

            return repositoryDirectory;

        } catch (TransportException e) {

            String message = e.getMessage();

            if (message != null &&
                    (message.contains("not found")
                    || message.contains("Repository not found")
                    || message.contains("remote: Repository not found"))) {

                throw new RepositoryNotFoundException(
                        "GitHub repository not found: " + repositoryUrl,
                        e
                );
            }

            throw new RuntimeException(
                    "Failed to clone repository: " + repositoryUrl,
                    e
            );

        } catch (Exception e) {

            System.err.println("JGit clone failed!");

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to clone repository: " +
                    repositoryUrl,
                    e
            );
        }
    }
}