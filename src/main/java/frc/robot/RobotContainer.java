// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController.Axis;
// import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController.Button;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.commands.*;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.LEDSubsystem;
import frc.robot.subsystems.VisionSubsystem;
import java.util.List;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems

  private final DriveSubsystem m_robotDrive = new DriveSubsystem();
  private final LEDSubsystem m_LEDSubsystem = new LEDSubsystem();
  private final ArmSubsystem m_robotArm = new ArmSubsystem();
  private final VisionSubsystem m_visionSubsystem = new VisionSubsystem();

  // The driver's controller
  /// Joystick m_driverJoystick = new Joystick(OIConstants.kDriverJoystickPort);
  XboxController m_driverController = new XboxController(OIConstants.kDriverControllerPort);

  SendableChooser<Command> m_chooser = new SendableChooser<>();

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();
    //    aButton.whileTrue(
    //         new ArmExtendMedCommand());

    //     bButton.whileTrue(
    //         new ArmExtendHighCommand());

    // Configure default commands
    m_robotDrive.setDefaultCommand(
        // The left stick controls translation of the robot.
        // Turning is controlled by the X axis bof the right stick.

        new RunCommand(
            () ->
                m_robotDrive.drive(
                    -MathUtil.applyDeadband(
                            m_driverController.getLeftY(), OIConstants.kDriveDeadband)
                        * GetSpeed(),
                    -MathUtil.applyDeadband(
                            m_driverController.getLeftX(), OIConstants.kDriveDeadband)
                        * GetSpeed(),
                    -MathUtil.applyDeadband(
                            m_driverController.getRightX(), OIConstants.kDriveDeadband)
                        * GetSpeed(),
                    true,
                    true),
            m_robotDrive));

    m_chooser.setDefaultOption(
        "Autonomous Score and Balance",
        new SequentialCommandGroup(
            new BalanceGyroSetZeroCommand(m_robotDrive),
            new AutoGridAlignCommand(m_robotDrive, m_visionSubsystem),
            new ArmMaxHeightCommand(m_robotArm),
            new ArmExtendCommand(m_robotArm),
            new ClawReleaseCommand(m_robotArm),
            new AutoDriveToBalanceCommand(m_robotDrive, m_visionSubsystem),
            new AutoBalanceCommand(m_robotDrive, m_visionSubsystem)));

    m_chooser.addOption(
        "Autonomous Score & Pick Up Cone",
        new SequentialCommandGroup(
            new BalanceGyroSetZeroCommand(m_robotDrive),
            new AutoGridAlignCommand(m_robotDrive, m_visionSubsystem),
            new ArmMaxHeightCommand(m_robotArm),
            new ArmExtendCommand(m_robotArm),
            new ClawReleaseCommand(m_robotArm),
            new AutoGetConeCommand(m_robotDrive, m_visionSubsystem)));

    m_chooser.addOption(
        "Autonomous Score & Pick Up Cube",
        new SequentialCommandGroup(
            new BalanceGyroSetZeroCommand(m_robotDrive),
            new AutoGridAlignCommand(m_robotDrive, m_visionSubsystem),
            new ArmMaxHeightCommand(m_robotArm),
            new ArmExtendCommand(m_robotArm),
            new ClawReleaseCommand(m_robotArm),
            new AutoGetCubeCommand(m_robotDrive, m_visionSubsystem)));

    m_chooser.addOption(
        "Autonomous Score Only",
        new SequentialCommandGroup(
            new BalanceGyroSetZeroCommand(m_robotDrive),
            new AutoGridAlignCommand(m_robotDrive, m_visionSubsystem),
            new ArmMaxHeightCommand(m_robotArm),
            new ArmExtendCommand(m_robotArm),
            new ClawReleaseCommand(m_robotArm)));

    SmartDashboard.putData("Auto Mode", m_chooser);

    /*m_robotDrive.drive(
                -MathUtil.applyDeadband(m_driverJoystick.getY(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverJoystick.getX(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(
                    m_driverJoystick.getThrottle(), OIConstants.kDriveDeadband),
                true,
                true),
    m_robotDrive));*/

    m_LEDSubsystem.setDefaultCommand(
        // The robot will display the scrolling purple lights by default
        new SetLEDProximityCommand(m_visionSubsystem, m_LEDSubsystem));
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling passing it to a
   * {@link JoystickButton}.
   *
   * @return
   */
  private void configureButtonBindings() {
    /*new JoystickButton(m_driverJoystick, 2)
        .whileTrue(new RunCommand(() -> m_robotDrive.setX(), m_robotDrive));

    // The buttons created below were meant for LED testing, feel free to change

    new JoystickButton(m_driverJoystick, 3)
        .whileTrue(new RunCommand(() -> m_LEDSubsystem.WarningLight(), m_LEDSubsystem));

    new JoystickButton(m_driverJoystick, 4)
        .whileTrue(new RunCommand(() -> m_LEDSubsystem.ReadyLight(), m_LEDSubsystem));

    new JoystickButton(m_driverJoystick, 5)
        .whileTrue(new RunCommand(() -> m_LEDSubsystem.LightOff(), m_LEDSubsystem));*/

    new JoystickButton(m_driverController, Button.kRightBumper.value)
        .whileTrue(new RunCommand(() -> m_robotDrive.setX(), m_robotDrive));

    new JoystickButton(m_driverController, Button.kX.value)
        .whileTrue(
            new SequentialCommandGroup(
                new ArmMidHeightCommand(m_robotArm), new ArmExtendCommand(m_robotArm)));

    new JoystickButton(m_driverController, Button.kX.value)
        .whileFalse(
            new SequentialCommandGroup(
                new ArmRetractCommand(m_robotArm), new ArmDefaultHeightCommand(m_robotArm)));

    new JoystickButton(m_driverController, Button.kY.value)
        .whileTrue(
            new SequentialCommandGroup(
                new ArmMaxHeightCommand(m_robotArm), new ArmExtendCommand(m_robotArm)));
    new JoystickButton(m_driverController, Button.kY.value)
        .whileFalse(
            new SequentialCommandGroup(
                new ArmRetractCommand(m_robotArm), new ArmDefaultHeightCommand(m_robotArm)));

    new JoystickButton(m_driverController, Button.kStart.value)
        .whileTrue(new ArmTestCommand(m_robotArm));
    new JoystickButton(m_driverController, Button.kStart.value)
        .whileFalse(new ArmTestOffCommand(m_robotArm));

    new JoystickButton(m_driverController, Button.kBack.value)
        .whileTrue(new ArmExtendCommand(m_robotArm));
    new JoystickButton(m_driverController, Button.kBack.value)
        .whileFalse(new ArmRetractCommand(m_robotArm));

    new JoystickButton(m_driverController, Button.kA.value)
        .whileTrue(new ClawGrabCommand(m_robotArm));

    new JoystickButton(m_driverController, Button.kA.value)
        .whileFalse(new ClawReleaseCommand(m_robotArm));

    new JoystickButton(m_driverController, Button.kLeftBumper.value)
        .onTrue(new BalanceGyroSetZeroCommand(m_robotDrive));

    new JoystickButton(m_driverController, Axis.kLeftTrigger.value)
        .whileTrue(new AutoBalanceCommand(m_robotDrive, m_visionSubsystem));

    new POVButton(m_driverController, -1)
        .whileFalse(new DriveDpadSneakCommand(m_robotDrive, m_driverController));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // Create config for trajectory
    TrajectoryConfig config =
        new TrajectoryConfig(
                AutoConstants.kMaxSpeedMetersPerSecond,
                AutoConstants.kMaxAccelerationMetersPerSecondSquared)
            // Add kinematics to ensure max speed is actually obeyed
            .setKinematics(DriveConstants.kDriveKinematics);

    // An example trajectory to follow. All units in meters.
    Trajectory exampleTrajectory =
        TrajectoryGenerator.generateTrajectory(
            // Start at the origin facing the +X direction
            new Pose2d(0, 0, new Rotation2d(0)),
            // Pass through these two interior waypoints, making an 's' curve path
            List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
            // End 3 meters straight ahead of where we started, facing forward
            new Pose2d(3, 0, new Rotation2d(0)),
            config);

    var thetaController =
        new ProfiledPIDController(
            AutoConstants.kPThetaController, 0, 0, AutoConstants.kThetaControllerConstraints);
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    SwerveControllerCommand swerveControllerCommand =
        new SwerveControllerCommand(
            exampleTrajectory,
            m_robotDrive::getPose, // Functional interface to feed supplier
            DriveConstants.kDriveKinematics,

            // Position controllers
            new PIDController(AutoConstants.kPXController, 0, 0),
            new PIDController(AutoConstants.kPYController, 0, 0),
            thetaController,
            m_robotDrive::setModuleStates,
            m_robotDrive);

    // Reset odometry to the starting pose of the trajectory.
    m_robotDrive.resetOdometry(exampleTrajectory.getInitialPose());

    // Run path following command, then stop at the end.
    return swerveControllerCommand.andThen(() -> m_robotDrive.drive(0, 0, 0, false, false));
  }

  public Command getAutonomousPowerStation() {
    return null;
  }

  public Command getAutonomousShotAndPowerStation() {
    return null;
  }

  public Command getAutonomousDoubleShot() {
    return null;
  }

  private double GetSpeed() {
    if (m_driverController.getAButton()) {
      return DriveConstants.kSprintSpeed;
    } else {
      return DriveConstants.kDefaultSpeed;
    }
  }
}
