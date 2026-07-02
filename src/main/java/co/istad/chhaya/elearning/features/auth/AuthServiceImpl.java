package co.istad.chhaya.elearning.features.auth;

import co.istad.chhaya.elearning.config.props.KeycloakAdminClientProps;
import co.istad.chhaya.elearning.features.auth.dto.RegisterRequest;
import co.istad.chhaya.elearning.features.auth.dto.RegisterResponse;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    private final Keycloak keycloak;
    private final KeycloakAdminClientProps props;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {

        // Validate password match or not
        if (!registerRequest.password().equals(registerRequest.confirmedPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords don't match"
            );
        }

        // Register user into Keycloak
        UserRepresentation user = new UserRepresentation();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setFirstName(registerRequest.firstName());
        user.setLastName(registerRequest.lastName());

        // Set custom attributes
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("gender", List.of(registerRequest.gender().getGender()));
        attributes.put("biography", List.of(registerRequest.biography()));
        user.setAttributes(attributes);

        // Keycloak system data
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Set credential or password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType("password");
        credential.setValue(registerRequest.password());
        user.setCredentials(List.of(credential));

        // Call Keycloak API
        UsersResource usersResource = keycloak
                .realm(props.getTargetRealm())
                .users();

        try (Response response = usersResource.create(user)) {
            log.info("Response status code: {}", response.getStatus());
            if (response.getStatus() == HttpStatus.CREATED.value()) {
                UserRepresentation createdUser = usersResource
                        .search(user.getUsername())
                        .getFirst();
                log.info("Created user {}", createdUser.getId());

                return RegisterResponse.builder()
                        .id(createdUser.getId())
                        .username(createdUser.getUsername())
                        .email(createdUser.getEmail())
                        .firstName(createdUser.getFirstName())
                        .lastName(createdUser.getLastName())
                        .gender(createdUser.getAttributes().get("gender").getFirst())
                        .biography(createdUser.getAttributes().get("biography").getFirst())
                        .build();
            }
        }

        return null;
    }

}
