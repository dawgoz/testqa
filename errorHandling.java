package sample;

public class ErrorHandling {

  
  public Object createUser(User user){
      
      if(loadUser(user.getId()) instanceof User){
        // user already exists
        return USER_ALREADY_EXISTS; // error flag
        return 101; // or error code
      };
      
      resolveGroup(user);
      return userRepository.save(user);
  }
  
  public void handleSimulation(Simulation simulation){
    try{
      resultHandler.handle(simulation);
    }
    catch (Throwable t){
      log.error("failed to save result", t);
    }
  }
  
    public void validateOwner(String ownerId){
    if(ownerRepository.countTestcasesForOwner(ownerId) == 0){
      throw new ValidationException("no valid owner...")
    }
  }
    
  public User createUser(User user){
      
      if(loadUser(user.getId()) instanceof User){
        // user already exists
        return null;
      };
      
      resolveGroup(user);
      return userRepository.save(user);
  }
  // passing null
    public User editUser(String userId, User user){
      
      if(userId == null){
        // new user
        throw new UserException("new user can't be edited...");
      };
      return userRepository.update(user);
  }
}
