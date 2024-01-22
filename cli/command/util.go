package command

import (
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
)

const INIT = "init"
const DEV = "dev"
const BUILD = "build"

func StopProcessTree(pid int) error {
	cmd := exec.Command("taskkill", "/F", "/T", "/PID", fmt.Sprint(pid))
	return cmd.Run()
}

func CommandWithLog(cmd string, args ...string) *exec.Cmd {
	c := exec.Command(cmd, args...)
	c.Stderr = os.Stderr
	c.Stdout = os.Stdout
	return c
}

func PrintFilePaths(folderPath string) error {
	var list []string
	err := filepath.Walk(folderPath, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		// Skip directories
		if info.IsDir() {
			return nil
		}
		// Print the file path relative to the input folder
		relPath, err := filepath.Rel(folderPath, path)
		if err != nil {
			return err
		}
		list = append(list, relPath)
		return nil
	})

	if err != nil {
		return fmt.Errorf("failed to walk folder: %v", err)
	}

	return nil
}

func MoveDirectory(source, destination string) error {
	// Create the destination directory if it doesn't exist
	if err := os.MkdirAll(destination, os.ModePerm); err != nil {
		return fmt.Errorf("failed to create destination directory: %v", err)
	}

	// Walk through the source directory and move files/directories
	err := filepath.Walk(source, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// Calculate the destination path for the current item
		destPath := filepath.Join(destination, path[len(source):])

		// If it's a directory, create it in the destination
		if info.IsDir() {
			if err := os.MkdirAll(destPath, os.ModePerm); err != nil {
				return fmt.Errorf("failed to create destination directory: %v", err)
			}
		} else {
			// If it's a file, move it to the destination
			if err := moveFile(path, destPath); err != nil {
				return fmt.Errorf("failed to move file: %v", err)
			}
		}

		return nil
	})

	if err != nil {
		return fmt.Errorf("failed to move directory: %v", err)
	}

	fmt.Printf("Directory '%s' successfully moved to '%s'\n", source, destination)
	return nil
}

func moveFile(source, destination string) error {
	sourceFile, err := os.Open(source)
	if err != nil {
		return err
	}
	defer sourceFile.Close()

	destFile, err := os.Create(destination)
	if err != nil {
		return err
	}
	defer destFile.Close()

	_, err = io.Copy(destFile, sourceFile)
	if err != nil {
		return err
	}

	return nil
}
