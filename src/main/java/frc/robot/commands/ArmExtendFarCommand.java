package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.ArmSubsystem;

public class ArmExtendFarCommand extends CommandBase {

  @Override
  public void initialize() {}

  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final ArmSubsystem m_Arm;

  public ArmExtendFarCommand(ArmSubsystem subsystem) {
    m_Arm = subsystem;
    addRequirements(subsystem);
  }

  @Override
  public void execute() {
    m_Arm.ArmExtendFarCommand();
  }

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public void end(boolean interupted) {
    m_Arm.ArmRetractCommand();
  }
}
