//go:generate goversioninfo -icon=mp4.ico -manifest=goversioninfo.exe.manifest -64
package main

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"syscall"
)

func main() {

	path, err := os.Getwd()
	if err != nil {
		log.Println(err)
	}

	java := path + "\\jre\\bin\\java.exe"
	fmt.Println(java)

	jar := path + "\\converter.jar"
	fmt.Println(jar)

	cmd := exec.Command(java, "-jar", jar)
	cmd.SysProcAttr = &syscall.SysProcAttr{CreationFlags: 0x08000000}

	stdout, err := cmd.Output()

	if err != nil {
		fmt.Println(err.Error())
		return
	}

	fmt.Println(string(stdout))
}
