package com.example.jpiet;

public interface CommandRunListener {
    public void onRunCommand(final Command command, final PietMachineStack stack);
}
