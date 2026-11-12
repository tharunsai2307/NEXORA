package com.smartbuilding.config;

import com.smartbuilding.building.model.*;
import com.smartbuilding.building.repository.BuildingRepository;
import com.smartbuilding.building.repository.BuildingUserAssignmentRepository;
import com.smartbuilding.building.repository.FloorRepository;
import com.smartbuilding.building.repository.RoomRepository;
import com.smartbuilding.sensor.model.*;
import com.smartbuilding.sensor.repository.SensorRepository;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.model.User;
import com.smartbuilding.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initializes baseline demographic and spatial seed data on application startup
 * if the database is unpopulated (e.g. in standalone in-memory mode or fresh instances).
 */
@Component
public class DatabaseDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseDataInitializer.class);

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final SensorRepository sensorRepository;
    private final BuildingUserAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseDataInitializer(
            UserRepository userRepository,
            BuildingRepository buildingRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            SensorRepository sensorRepository,
            BuildingUserAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.sensorRepository = sensorRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[NEXORA Init] Checking database initialization status...");

        if (userRepository.count() > 0 && buildingRepository.count() > 0) {
            log.info("[NEXORA Init] Database already contains initialized data. Skipping seed.");
            return;
        }

        log.info("[NEXORA Init] Database empty or partial. Seeding baseline entities...");

        // 1. Seed Users
        String encodedPassword = passwordEncoder.encode("NexoraAdmin2026!");

        User admin = userRepository.save(new User(
                "u-admin-001", "System Administrator", "admin@nexora.com",
                encodedPassword, "+1-555-0100", Role.SYSTEM_ADMIN, true));

        User manager = userRepository.save(new User(
                "u-mgr-002", "Facilities Manager", "manager@nexora.com",
                encodedPassword, "+1-555-0101", Role.BUILDING_MANAGER, true));

        User safety = userRepository.save(new User(
                "u-sft-003", "Chief Safety Officer", "safety@nexora.com",
                encodedPassword, "+1-555-0102", Role.SAFETY_OFFICER, true));

        log.info("[NEXORA Init] Seeded 3 default users (Admin, Manager, Safety) with password 'NexoraAdmin2026!'.");

        // 2. Seed Building
        Building building = new Building(
                "b-tower-001",
                "NEXORA Innovation Tower",
                "NEX-TWR-01",
                "Flagship smart commercial research and innovation facility with multi-zone environmental sensors.",
                "100 Silicon Boulevard, Tech District",
                BuildingType.COMMERCIAL,
                BuildingStatus.ACTIVE
        );
        building = buildingRepository.save(building);

        // Assign users to building
        assignmentRepository.save(new BuildingUserAssignment("bua-001", building, manager));
        assignmentRepository.save(new BuildingUserAssignment("bua-002", building, safety));

        // 3. Seed Floors
        Floor fl0 = floorRepository.save(new Floor("fl-001", building, "Ground Floor (Lobby & Amenities)", 0,
                "Main entrance reception, cafeteria, security checkpoint, and conference atrium.", 70.0, 50.0, FloorStatus.ACTIVE));

        Floor fl1 = floorRepository.save(new Floor("fl-002", building, "First Floor (Offices & Labs)", 1,
                "Engineering workstations, software development labs, and collaborative team zones.", 70.0, 50.0, FloorStatus.ACTIVE));

        Floor fl2 = floorRepository.save(new Floor("fl-003", building, "Second Floor (Executive & Data Center)", 2,
                "Executive boardrooms, server datacenter room, and network operations center.", 70.0, 50.0, FloorStatus.ACTIVE));

        // 4. Seed Rooms
        List<Room> rooms = List.of(
                new Room("rm-101", fl0, "Main Reception & Atrium", "L0-ATRIUM", RoomType.LOBBY,
                        "Central ground-floor atrium with visitor registration kiosks.", 80, 15, SafetyStatus.NORMAL,
                        0.0, 0.0, 0.0, 20.0, 15.0, 4.5, true),
                new Room("rm-102", fl0, "Central Conference Hall", "L0-CONF-A", RoomType.CONFERENCE_ROOM,
                        "High-capacity multimedia presentation hall.", 50, 0, SafetyStatus.NORMAL,
                        25.0, 0.0, 0.0, 15.0, 12.0, 3.8, true),
                new Room("rm-103", fl0, "Cafeteria & Lounge", "L0-CAFE", RoomType.CAFETERIA,
                        "Dining area with kitchen facilities and breakout seating.", 60, 22, SafetyStatus.NORMAL,
                        -25.0, 0.0, 0.0, 18.0, 14.0, 3.8, true),
                new Room("rm-201", fl1, "AI & Robotics Lab", "L1-LAB-AI", RoomType.LABORATORY,
                        "Embedded hardware testing and edge computing research facility.", 30, 8, SafetyStatus.NORMAL,
                        -15.0, 0.0, 4.0, 14.0, 12.0, 3.5, true),
                new Room("rm-202", fl1, "Open Workspace Alpha", "L1-WORK-A", RoomType.OFFICE,
                        "Collaborative agile software development open office.", 45, 18, SafetyStatus.NORMAL,
                        15.0, 0.0, 4.0, 18.0, 14.0, 3.5, true),
                new Room("rm-301", fl2, "Primary Datacenter & Server Room", "L2-SRV-01", RoomType.SERVER_ROOM,
                        "High-density rack server room with dedicated precision cooling.", 8, 2, SafetyStatus.NORMAL,
                        0.0, 0.0, 8.0, 12.0, 10.0, 3.5, true),
                new Room("rm-302", fl2, "Executive Boardroom", "L2-EXEC-01", RoomType.CONFERENCE_ROOM,
                        "Boardroom equipped with telepresence and spatial acoustic panels.", 20, 5, SafetyStatus.NORMAL,
                        20.0, 0.0, 8.0, 12.0, 10.0, 3.5, true)
        );

        for (Room r : rooms) {
            Room savedRoom = roomRepository.save(r);

            // 5. Seed 5 Sensors per room
            sensorRepository.save(new Sensor(savedRoom, "SNS-" + savedRoom.getRoomCode() + "-TEMP",
                    savedRoom.getRoomName() + " Temp Sensor", SensorType.TEMPERATURE, SensorUnit.CELSIUS));

            sensorRepository.save(new Sensor(savedRoom, "SNS-" + savedRoom.getRoomCode() + "-HUM",
                    savedRoom.getRoomName() + " Humidity Sensor", SensorType.HUMIDITY, SensorUnit.PERCENT));

            sensorRepository.save(new Sensor(savedRoom, "SNS-" + savedRoom.getRoomCode() + "-SMK",
                    savedRoom.getRoomName() + " Smoke Detector", SensorType.SMOKE, SensorUnit.PPM));

            sensorRepository.save(new Sensor(savedRoom, "SNS-" + savedRoom.getRoomCode() + "-AQI",
                    savedRoom.getRoomName() + " Air Quality Sensor", SensorType.AIR_QUALITY, SensorUnit.AQI));

            sensorRepository.save(new Sensor(savedRoom, "SNS-" + savedRoom.getRoomCode() + "-OCC",
                    savedRoom.getRoomName() + " Occupancy Counter", SensorType.OCCUPANCY, SensorUnit.PEOPLE));
        }

        log.info("[NEXORA Init] Seeded Flagship Building, 3 Floors, 7 Rooms, and 35 Virtual Sensors successfully.");
    }
}
