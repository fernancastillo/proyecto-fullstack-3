package com.userservice.config;

import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // ─── ADMIN ───────────────────────────────────────────────────
        if (!userRepository.existsByEmail("admin@rednorte.cl")) {
            userRepository.save(new User(null, "11111111", "1",
                "Administrador", "Sistema", "admin@rednorte.cl",
                passwordEncoder.encode("admin1234"), "+56911111111",
                "Tarapacá", "Iquique", "Av. Principal 100", "ADMIN", null));
        }

        // ─── CARDIOLOGÍA ─────────────────────────────────────────────
        if (!userRepository.existsByEmail("dr.garcia@rednorte.cl")) {
            userRepository.save(new User(null, "22222222", "2",
                "Carlos", "García", "dr.garcia@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56922222222",
                "Tarapacá", "Iquique", "Av. Arturo Prat 200", "MEDICO", "Cardiología"));
        }

        if (!userRepository.existsByEmail("dra.fuentes@rednorte.cl")) {
            userRepository.save(new User(null, "23232323", "2",
                "Valentina", "Fuentes", "dra.fuentes@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56923232323",
                "Tarapacá", "Iquique", "Calle Balmaceda 210", "MEDICO", "Cardiología"));
        }

        // ─── PEDIATRÍA ───────────────────────────────────────────────
        if (!userRepository.existsByEmail("dra.lopez@rednorte.cl")) {
            userRepository.save(new User(null, "33333333", "3",
                "Ana", "López", "dra.lopez@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56933333333",
                "Tarapacá", "Alto Hospicio", "Calle Hospital 300", "MEDICO", "Pediatría"));
        }

        if (!userRepository.existsByEmail("dr.navarro@rednorte.cl")) {
            userRepository.save(new User(null, "34343434", "3",
                "Rodrigo", "Navarro", "dr.navarro@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56934343434",
                "Tarapacá", "Alto Hospicio", "Pasaje Las Flores 320", "MEDICO", "Pediatría"));
        }

        // ─── TRAUMATOLOGÍA ───────────────────────────────────────────
        if (!userRepository.existsByEmail("dr.martinez@rednorte.cl")) {
            userRepository.save(new User(null, "44444444", "4",
                "Pedro", "Martínez", "dr.martinez@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56944444444",
                "Antofagasta", "Antofagasta", "Av. Salvador Allende 400", "MEDICO", "Traumatología"));
        }

        if (!userRepository.existsByEmail("dra.rios@rednorte.cl")) {
            userRepository.save(new User(null, "45454545", "4",
                "Catalina", "Ríos", "dra.rios@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56945454545",
                "Antofagasta", "Antofagasta", "Calle Prat 410", "MEDICO", "Traumatología"));
        }

        // ─── NEUROLOGÍA ──────────────────────────────────────────────
        if (!userRepository.existsByEmail("dra.torres@rednorte.cl")) {
            userRepository.save(new User(null, "55555555", "5",
                "Gabriela", "Torres", "dra.torres@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56955555555",
                "Antofagasta", "Calama", "Calle Los Andes 500", "MEDICO", "Neurología"));
        }

        if (!userRepository.existsByEmail("dr.vega@rednorte.cl")) {
            userRepository.save(new User(null, "56565656", "5",
                "Andrés", "Vega", "dr.vega@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56956565656",
                "Antofagasta", "Calama", "Av. O'Higgins 510", "MEDICO", "Neurología"));
        }

        // ─── MEDICINA GENERAL ────────────────────────────────────────
        if (!userRepository.existsByEmail("dr.castillo@rednorte.cl")) {
            userRepository.save(new User(null, "66666666", "6",
                "Felipe", "Castro", "dr.castro@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56966666666",
                "Tarapacá", "Iquique", "Av. Zañartu 600", "MEDICO", "Medicina General"));
        }

        if (!userRepository.existsByEmail("dra.mora@rednorte.cl")) {
            userRepository.save(new User(null, "67676767", "6",
                "Daniela", "Mora", "dra.mora@rednorte.cl",
                passwordEncoder.encode("medico1234"), "+56967676767",
                "Tarapacá", "Iquique", "Calle Serrano 610", "MEDICO", "Medicina General"));
        }

        // ─── PACIENTES ───────────────────────────────────────────────
        if (!userRepository.existsByEmail("juan.perez@correo.cl")) {
            userRepository.save(new User(null, "77777777", "7",
                "Juan", "Pérez", "juan.perez@correo.cl",
                passwordEncoder.encode("paciente1234"), "+56977777777",
                "Tarapacá", "Iquique", "Pasaje Los Pinos 10", "PACIENTE", null));
        }

        if (!userRepository.existsByEmail("maria.gonzalez@correo.cl")) {
            userRepository.save(new User(null, "88888888", "8",
                "María", "González", "maria.gonzalez@correo.cl",
                passwordEncoder.encode("paciente1234"), "+56988888888",
                "Antofagasta", "Antofagasta", "Villa El Sol 20", "PACIENTE", null));
        }

        if (!userRepository.existsByEmail("pedro.silva@correo.cl")) {
            userRepository.save(new User(null, "99999999", "9",
                "Pedro", "Silva", "pedro.silva@correo.cl",
                passwordEncoder.encode("paciente1234"), "+56999999999",
                "Tarapacá", "Alto Hospicio", "Población Nueva 30", "PACIENTE", null));
        }

        if (!userRepository.existsByEmail("sofia.rojas@correo.cl")) {
            userRepository.save(new User(null, "10101010", "K",
                "Sofía", "Rojas", "sofia.rojas@correo.cl",
                passwordEncoder.encode("paciente1234"), "+56910101010",
                "Antofagasta", "Calama", "Calle Nueva 40", "PACIENTE", null));
        }

        if (!userRepository.existsByEmail("lucas.mendez@correo.cl")) {
            userRepository.save(new User(null, "12121212", "K",
                "Lucas", "Méndez", "lucas.mendez@correo.cl",
                passwordEncoder.encode("paciente1234"), "+56912121212",
                "Antofagasta", "Antofagasta", "Av. Grecia 50", "PACIENTE", null));
        }
    }
}